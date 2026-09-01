package com.carestock;

import com.carestock.dao.MedicamentoDAO;
import com.carestock.model.Medicamento;

import java.util.List;
import java.util.Scanner;

public class MainApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        MedicamentoDAO dao = new MedicamentoDAO();
        boolean salir = false;

        while (!salir) {
            System.out.println("\n=========================================");
            System.out.println("   CARESTOCK - MÓDULO DE INVENTARIO (JAVA)");
            System.out.println("=========================================");
            System.out.println("1. Mostrar Medicamentos (Desde Neon)");
            System.out.println("2. Agregar Nuevo Medicamento (Guardar en Neon)");
            System.out.println("3. Salir");
            System.out.print("Seleccione una opción: ");

            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    List<Medicamento> lista = dao.obtenerTodos();
                    System.out.println("\n--- LISTA DE MEDICAMENTOS ---");
                    if (lista.isEmpty()) {
                        System.out.println("No hay medicamentos registrados en Neon.");
                    } else {
                        System.out.println("ID | INVIMA | NOMBRE | PRINCIPIO | CATEGORIA | STOCK");
                        for (Medicamento m : lista) {
                            System.out.printf("%d | %s | %s | %s | %s | %d\n",
                                m.getIdMedicamento(), m.getCodigoInvima(), m.getNombreComercial(),
                                m.getPrincipioActivo(), m.getCategoria(), m.getStockTotal());
                        }
                    }
                    break;

                case "2":
                    System.out.println("\n--- REGISTRAR EN NEON CLOUD ---");
                    System.out.print("Código INVIMA: ");
                    String codigo = scanner.nextLine();
                    System.out.print("Nombre Comercial: ");
                    String nombre = scanner.nextLine();
                    System.out.print("Principio Activo: ");
                    String principio = scanner.nextLine();
                    System.out.print("Concentración: ");
                    String concentracion = scanner.nextLine();
                    System.out.print("Categoría (ANALGESICOS, ANTIBIOTICOS, CARDIOVASCULAR): ");
                    String categoria = scanner.nextLine();
                    System.out.print("Stock Total: ");
                    int stockTotal = Integer.parseInt(scanner.nextLine());
                    System.out.print("Stock Mínimo: ");
                    int stockMinimo = Integer.parseInt(scanner.nextLine());

                    Medicamento nuevo = new Medicamento(0L, codigo, nombre, principio, concentracion, categoria, stockTotal, stockMinimo);
                    
                    if (dao.agregarMedicamento(nuevo)) {
                        System.out.println("✅ ¡Medicamento guardado e insertado exitosamente en Neon Cloud!");
                    } else {
                        System.out.println("❌ Error al guardar el medicamento.");
                    }
                    break;

                case "3":
                    salir = true;
                    System.out.println("Saliendo del sistema...");
                    break;

                default:
                    System.out.println("Opción inválida.");
            }
        }
    }
}
