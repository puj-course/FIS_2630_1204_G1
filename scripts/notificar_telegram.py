"""Publica eventos de GitHub Actions en Telegram."""

import json
import os
import sys
from datetime import datetime, timedelta, timezone
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

REPO = os.environ.get(
    "GITHUB_REPOSITORY",
    "puj-course/FIS_2630_1204_G1"
)
ACTOR = os.environ.get("GITHUB_ACTOR", "GitHub")
EVENT = os.environ.get(
    "GITHUB_EVENT_NAME",
    "workflow_dispatch"
)
BASE_URL = f"https://github.com/{REPO}"


def clean(value, limit=130):
    s = " ".join(str(value or "").split())
    if len(s) <= limit:
        return s
    return s[:limit - 3] + "..."


def author(commit):
    data = commit.get("author") or {}
    return clean(
        data.get("name")
        or data.get("username")
        or "sin autor",
        50
    )


def build_message(event):
    action = event.get("action", "")
    actor = (
        (event.get("sender") or {}).get("login")
        or ACTOR
    )

    lines = [
        "\U0001F4E6 CareStock | Actividad GitHub",
        f"Usuario: {actor}"
    ]
    url = BASE_URL

    if EVENT == "push":
        branch = (
            event.get("ref", "")
            .removeprefix("refs/heads/")
            .removeprefix("refs/tags/")
        )
        commits = event.get("commits") or []

        lines += [
            "\U0001F680 PUSH",
            f"Rama/tag: {clean(branch)}",
            f"Commits: {len(commits)}"
        ]

        for commit in commits[:5]:
            lines.append(
                f"- {author(commit)}: "
                f"{clean(commit.get('message'), 110)}"
            )

        if len(commits) > 5:
            lines.append(
                f"... y {len(commits) - 5} commits mas"
            )

        url = event.get("compare") or BASE_URL + "/commits"

    elif EVENT == "pull_request":
        pr = event["pull_request"]

        if action == "closed" and pr.get("merged"):
            verb = "MERGE"
        else:
            verb = f"PR {action}"

        lines += [
            f"\U0001F500 {verb} #{pr['number']}",
            f"Titulo: {clean(pr.get('title'))}",
            f"Ramas: {pr['head']['ref']} -> "
            f"{pr['base']['ref']}",
            f"Autor del PR: {pr['user']['login']}"
        ]

        if verb == "MERGE":
            merger = (
                (pr.get("merged_by") or {}).get("login")
            )
            if merger:
                lines.append(f"Merge por: {merger}")

        if action in ("assigned", "unassigned"):
            user = (
                (event.get("assignee") or {})
                .get("login", "-")
            )
            lines.append(f"Asignado: {user}")

        if action in ("labeled", "unlabeled"):
            label = (
                (event.get("label") or {})
                .get("name", "-")
            )
            lines.append(f"Etiqueta: {label}")

        url = pr["html_url"]

    elif EVENT == "pull_request_review":
        pr = event["pull_request"]
        review = event.get("review") or {}

        lines += [
            f"\U0001F50D Revision PR #{pr['number']}",
            f"Decision: {review.get('state', action)}",
            f"Titulo: {clean(pr.get('title'))}"
        ]

        url = review.get("html_url") or pr["html_url"]

    elif EVENT == "pull_request_review_comment":
        pr = event["pull_request"]

        lines += [
            f"\U0001F4AC Comentario en codigo "
            f"del PR #{pr['number']}",
            f"Titulo: {clean(pr.get('title'))}"
        ]

        url = (
            (event.get("comment") or {})
            .get("html_url")
            or pr["html_url"]
        )

    elif EVENT == "issues":
        issue = event["issue"]

        lines += [
            f"\U0001F4CB Issue {action} "
            f"#{issue['number']}",
            f"Titulo: {clean(issue.get('title'))}"
        ]

        if action in ("assigned", "unassigned"):
            user = (
                (event.get("assignee") or {})
                .get("login", "-")
            )
            lines.append(f"Asignado: {user}")

        if action in ("labeled", "unlabeled"):
            label = (
                (event.get("label") or {})
                .get("name", "-")
            )
            lines.append(f"Etiqueta: {label}")

        url = issue["html_url"]

    elif EVENT == "issue_comment":
        issue = event["issue"]
        kind = (
            "PR" if issue.get("pull_request")
            else "Issue"
        )

        lines += [
            f"\U0001F4AC Comentario en "
            f"{kind} #{issue['number']}",
            f"Titulo: {clean(issue.get('title'))}"
        ]

        url = event["comment"]["html_url"]

    elif EVENT == "commit_comment":
        lines += [
            "\U0001F4AC Comentario en commit",
            f"Commit: {clean(event.get('commit_id'), 45)}"
        ]

        url = (
            (event.get("comment") or {})
            .get("html_url")
            or BASE_URL
        )

    elif EVENT in ("create", "delete"):
        lines += [
            f"\U0001F33F {EVENT.upper()} "
            f"{event.get('ref_type', '')}",
            f"Referencia: {clean(event.get('ref'))}"
        ]

    elif EVENT == "release":
        release = event["release"]

        lines += [
            "\U0001F3F7 Release publicada",
            f"Version: {clean(release.get('tag_name'))}"
        ]

        url = release["html_url"]

    elif EVENT == "workflow_run":
        run = event["workflow_run"]

        lines += [
            "\u2699 Workflow finalizado",
            f"Proceso: {clean(run.get('name'))}",
            f"Resultado: "
            f"{run.get('conclusion') or run.get('status')}",
            f"Rama: {clean(run.get('head_branch'))}",
            f"Iniciado por: "
            f"{(run.get('actor') or {}).get('login', '-')}"
        ]

        url = run["html_url"]

    elif EVENT == "workflow_dispatch":
        lines.append(
            "\U0001F9EA Prueba manual de notificaciones"
        )
        url = BASE_URL + "/actions"

    else:
        lines.append(f"Evento: {EVENT} {action}")

    now = datetime.now(
        timezone(timedelta(hours=-5))
    )

    lines += [
        f"Repositorio: {REPO}",
        f"Notificado: {now:%Y-%m-%d %H:%M} (Bogota)",
        f"Ver detalle: {url}"
    ]

    return "\n".join(lines)[:3950]


def main():
    dry_run = "--dry-run" in sys.argv

    path = os.environ.get("GITHUB_EVENT_PATH")

    event = (
        json.loads(
            Path(path).read_text(encoding="utf-8")
        )
        if path else {}
    )

    message = build_message(event)

    if dry_run:
        print(message)
        return 0

    token = os.environ.get("TELEGRAM_BOT_TOKEN")
    chat_id = os.environ.get("TELEGRAM_CHAT_ID")

    if not token or not chat_id:
        print(
            "::error::Falta TELEGRAM_BOT_TOKEN "
            "o TELEGRAM_CHAT_ID"
        )
        return 1

    data = json.dumps({
        "chat_id": chat_id,
        "text": message,
        "link_preview_options": {
            "is_disabled": True
        }
    }, ensure_ascii=True).encode("utf-8")

    req = Request(
        f"https://api.telegram.org/bot{token}/sendMessage",
        data=data,
        headers={
            "Content-Type": "application/json; charset=utf-8"
        },
        method="POST"
    )

    try:
        with urlopen(req, timeout=20) as result:
            response = json.load(result)

        if not response.get("ok"):
            print("::error::Telegram rechazo el mensaje")
            return 1

    except HTTPError as exc:
        print(
            f"::error::Error HTTP Telegram: {exc.code}"
        )
        return 1

    except (URLError, TimeoutError, ValueError) as exc:
        print(
            f"::error::Fallo envio Telegram: "
            f"{type(exc).__name__}"
        )
        return 1

    print(f"Evento {EVENT} notificado a Telegram")
    return 0


if __name__ == "__main__":
    sys.exit(main())
