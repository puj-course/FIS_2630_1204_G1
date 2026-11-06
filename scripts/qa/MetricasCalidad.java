import com.sun.source.tree.*;
import com.sun.source.util.*;
import javax.tools.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class MetricasCalidad {

    record Metodo(
        String archivo,
        String nombre,
        long linea,
        int loc,
        int cc,
        int profundidad
    ) {}

    record Archivo(String nombre, int loc) {}

    static class Complejidad extends TreeScanner<Void, Integer> {

        int cc = 1;
        int profundidad = 0;

        @Override
        public Void scan(Tree t, Integer d) {
            if (t == null) return null;

            Tree.Kind k = t.getKind();

            if (k == Tree.Kind.CLASS ||
                k == Tree.Kind.LAMBDA_EXPRESSION) {
                return null;
            }

            boolean decision = switch (k) {
                case FOR_LOOP, ENHANCED_FOR_LOOP,
                     WHILE_LOOP, DO_WHILE_LOOP,
                     CATCH, CONDITIONAL_EXPRESSION,
                     CONDITIONAL_AND, CONDITIONAL_OR -> true;

                case CASE -> ((CaseTree)t)
                    .getLabels()
                    .stream()
                    .anyMatch(x ->
                        x.getKind() != Tree.Kind.DEFAULT_CASE_LABEL
                    );

                default -> false;
            };

            if (decision) cc++;

            boolean control = switch (k) {
                case FOR_LOOP, ENHANCED_FOR_LOOP,
                     WHILE_LOOP, DO_WHILE_LOOP,
                     SWITCH, SWITCH_EXPRESSION,
                     CATCH, CONDITIONAL_EXPRESSION -> true;
                default -> false;
            };

            if (control) {
                profundidad = Math.max(profundidad, ++d);
            }

            return super.scan(t, d);
        }

        @Override
        public Void visitIf(IfTree t, Integer d) {
            cc++;
            profundidad = Math.max(profundidad, d + 1);

            scan(t.getCondition(), d);
            scan(t.getThenStatement(), d + 1);

            scan(
                t.getElseStatement(),
                t.getElseStatement() != null &&
                t.getElseStatement().getKind() == Tree.Kind.IF
                    ? d
                    : d + 1
            );

            return null;
        }
    }

    // LOC: lineas con codigo real.
    // Excluye lineas vacias, comentarios y contenido
    // interno de bloques de texto.

    static int loc(String s) {
        int total = 0;

        boolean bloque = false;
        boolean triple = false;
        boolean cadena = false;
        boolean caracter = false;
        boolean escape = false;
        boolean tiene = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            char sig = i + 1 < s.length()
                ? s.charAt(i + 1)
                : '\0';

            if (c == '\n') {
                if (tiene) total++;
                tiene = false;

                if (!triple) {
                    cadena = false;
                    caracter = false;
                    escape = false;
                }
                continue;
            }

            if (bloque) {
                if (c == '*' && sig == '/') {
                    bloque = false;
                    i++;
                }
                continue;
            }

            if (triple) {
                if (c == '"' && s.startsWith("\"\"\"", i)) {
                    triple = false;
                    tiene = true;
                    i += 2;
                }
                continue;
            }

            if (cadena || caracter) {
                tiene = true;

                if (escape) {
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (
                    (cadena && c == '"') ||
                    (caracter && c == '\'')
                ) {
                    cadena = false;
                    caracter = false;
                }
                continue;
            }

            if (c == '/' && sig == '/') {
                while (i < s.length() &&
                       s.charAt(i) != '\n') {
                    i++;
                }
                i--;
                continue;
            }

            if (c == '/' && sig == '*') {
                bloque = true;
                i++;
                continue;
            }

            if (c == '"' && s.startsWith("\"\"\"", i)) {
                triple = true;
                tiene = true;
                i += 2;
                continue;
            }

            if (c == '"') {
                cadena = true;
                tiene = true;
                continue;
            }

            if (c == '\'') {
                caracter = true;
                tiene = true;
                continue;
            }

            if (!Character.isWhitespace(c)) {
                tiene = true;
            }
        }

        return total + (tiene ? 1 : 0);
    }

    static String csv(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            throw new IllegalArgumentException(
                "Uso: java scripts/qa/MetricasCalidad.java " +
                "<src/main/java> <salida>"
            );
        }

        Path raiz = Path.of(args[0])
            .toAbsolutePath()
            .normalize();

        Path salida = Path.of(args[1]);

        Files.createDirectories(salida);

        List<Path> rutas;

        try (Stream<Path> f = Files.walk(raiz)) {
            rutas = f
                .filter(p -> p.toString().endsWith(".java"))
                .sorted()
                .toList();
        }

        if (rutas.isEmpty()) {
            throw new IllegalStateException(
                "Sin archivos Java de produccion"
            );
        }

        JavaCompiler javac =
            ToolProvider.getSystemJavaCompiler();

        if (javac == null) {
            throw new IllegalStateException(
                "Se requiere JDK 21"
            );
        }

        var errores =
            new DiagnosticCollector<JavaFileObject>();

        List<Metodo> metodos = new ArrayList<>();
        List<Archivo> archivos = new ArrayList<>();

        try (StandardJavaFileManager fm =
             javac.getStandardFileManager(
                 errores,
                 Locale.ROOT,
                 StandardCharsets.UTF_8
             )) {

            JavacTask tarea = (JavacTask) javac.getTask(
                null,
                fm,
                errores,
                List.of("-proc:none", "--release", "21"),
                null,
                fm.getJavaFileObjectsFromPaths(rutas)
            );

            SourcePositions posiciones =
                Trees.instance(tarea).getSourcePositions();

            for (CompilationUnitTree unidad : tarea.parse()) {

                Path ruta = Path.of(
                    unidad.getSourceFile().toUri()
                );

                String fuente = Files.readString(ruta);

                String archivo = raiz
                    .relativize(ruta)
                    .toString()
                    .replace('\\', '/');

                archivos.add(
                    new Archivo(archivo, loc(fuente))
                );

                new TreePathScanner<Void, Void>() {

                    Deque<String> clases = new ArrayDeque<>();

                    @Override
                    public Void visitClass(ClassTree t, Void x) {
                        clases.addLast(
                            t.getSimpleName().toString()
                        );

                        super.visitClass(t, x);
                        clases.removeLast();

                        return null;
                    }

                    @Override
                    public Void visitMethod(MethodTree t, Void x) {

                        if (t.getBody() != null) {

                            long a = posiciones
                                .getStartPosition(unidad, t);

                            long b = posiciones
                                .getEndPosition(unidad, t);

                            if (a >= 0 &&
                                b > a &&
                                b <= fuente.length()) {

                                Complejidad c = new Complejidad();
                                c.scan(t.getBody(), 0);

                                metodos.add(new Metodo(
                                    archivo,
                                    String.join(".", clases)
                                        + "." + t.getName(),
                                    unidad.getLineMap()
                                        .getLineNumber(a),
                                    loc(fuente.substring(
                                        (int)a, (int)b
                                    )),
                                    c.cc,
                                    c.profundidad
                                ));
                            }
                        }

                        return super.visitMethod(t, x);
                    }
                }.scan(unidad, null);
            }
        }

        for (var e : errores.getDiagnostics()) {
            if (e.getKind() == Diagnostic.Kind.ERROR) {
                throw new IllegalStateException(e.toString());
            }
        }

        metodos.sort(
            Comparator.comparingInt(Metodo::cc)
                .reversed()
                .thenComparing(
                    Comparator.comparingInt(
                        Metodo::profundidad
                    ).reversed()
                )
                .thenComparing(
                    Comparator.comparingInt(
                        Metodo::loc
                    ).reversed()
                )
        );

        StringBuilder csvMetodos = new StringBuilder(
            "archivo;metodo;linea;loc;ccn;anidamiento\n"
        );

        for (Metodo m : metodos) {
            csvMetodos
                .append(csv(m.archivo)).append(';')
                .append(csv(m.nombre)).append(';')
                .append(m.linea).append(';')
                .append(m.loc).append(';')
                .append(m.cc).append(';')
                .append(m.profundidad).append('\n');
        }

        Files.writeString(
            salida.resolve("metodos.csv"),
            csvMetodos,
            StandardCharsets.UTF_8
        );

        StringBuilder csvArchivos =
            new StringBuilder("archivo;loc\n");

        for (Archivo a : archivos) {
            csvArchivos
                .append(csv(a.nombre))
                .append(';')
                .append(a.loc)
                .append('\n');
        }

        Files.writeString(
            salida.resolve("archivos.csv"),
            csvArchivos,
            StandardCharsets.UTF_8
        );

        int total = archivos.stream()
            .mapToInt(Archivo::loc)
            .sum();

        double media = metodos.stream()
            .mapToInt(Metodo::cc)
            .average()
            .orElse(0);

        int maxCC = metodos.stream()
            .mapToInt(Metodo::cc)
            .max()
            .orElse(0);

        int maxProf = metodos.stream()
            .mapToInt(Metodo::profundidad)
            .max()
            .orElse(0);

        long alertas = metodos.stream()
            .filter(m ->
                m.cc > 10 ||
                m.loc > 60 ||
                m.profundidad > 3
            )
            .count();

        StringBuilder md = new StringBuilder(
            "# Metricas adicionales de CareStock\n\n"
        )
            .append("Analisis estatico de Java de produccion.\n\n")
            .append("| Indicador | Valor |\n")
            .append("|---|---:|\n")
            .append("| Archivos Java | ")
            .append(archivos.size()).append(" |\n")
            .append("| LOC | ")
            .append(total).append(" |\n")
            .append("| Metodos | ")
            .append(metodos.size()).append(" |\n")
            .append("| CCN media | ")
            .append(String.format(Locale.ROOT, "%.2f", media))
            .append(" |\n")
            .append("| CCN maxima | ")
            .append(maxCC).append(" |\n")
            .append("| Anidamiento maximo | ")
            .append(maxProf).append(" |\n")
            .append("| Metodos a revisar | ")
            .append(alertas).append(" |\n\n")
            .append("## Metodos prioritarios\n\n")
            .append("| Archivo y metodo | Linea | LOC | CCN | Anidamiento |\n")
            .append("|---|---:|---:|---:|---:|\n");

        for (Metodo m : metodos.stream().limit(15).toList()) {
            md.append('|')
                .append(m.archivo)
                .append(": ")
                .append(m.nombre)
                .append('|')
                .append(m.linea)
                .append('|')
                .append(m.loc)
                .append('|')
                .append(m.cc)
                .append('|')
                .append(m.profundidad)
                .append("|\n");
        }

        md.append(
            "\nUmbrales orientativos: CCN > 10, " +
            "LOC/metodo > 60 o anidamiento > 3.\n\n" +
            "No bloquean el CI. La medicion local " +
            "puede diferir de SonarQube.\n"
        );

        Files.writeString(
            salida.resolve("resumen.md"),
            md,
            StandardCharsets.UTF_8
        );

        System.out.printf(
            Locale.ROOT,
            "Archivos: %d | LOC: %d | Metodos: %d | " +
            "CCN media: %.2f | CCN max: %d | " +
            "Anidamiento max: %d | Alertas: %d%n",
            archivos.size(),
            total,
            metodos.size(),
            media,
            maxCC,
            maxProf,
            alertas
        );

        System.out.println("Reportes: " + salida);
    }
}
