import java.io.Console;
import java.util.Arrays;
import org.mindrot.jbcrypt.BCrypt;

public class GenerarHash {

    public static void main(String[] args) {

        Console console = System.console();

        if (console == null) {
            System.out.println(
                    "No fue posible abrir la consola segura."
            );
            return;
        }

        char[] password =
                console.readPassword(
                        "Nueva contraseña para admin@carestock.com: "
                );

        char[] confirmacion =
                console.readPassword(
                        "Confirme la nueva contraseña: "
                );

        String pwd =
                new String(password);

        String pwdConfirmacion =
                new String(confirmacion);

        if (!pwd.equals(pwdConfirmacion)) {

            System.out.println(
                    "Las contraseñas no coinciden."
            );

            Arrays.fill(password, '\0');
            Arrays.fill(confirmacion, '\0');

            return;
        }

        String hash =
                BCrypt.hashpw(
                        pwd,
                        BCrypt.gensalt(12)
                );

        System.out.println();
        System.out.println(
                "Hash BCrypt generado:"
        );
        System.out.println(hash);

        Arrays.fill(password, '\0');
        Arrays.fill(confirmacion, '\0');
    }
}
