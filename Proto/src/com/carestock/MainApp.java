package com.carestock;

import com.carestock.dao.MedicamentoDAO;
import com.carestock.model.Medicamento;

import java.util.List;

public class MainApp {

    public static void main(String[] args) {
        MedicamentoDAO dao = new MedicamentoDAO();
        List<Medicamento> medicamentos = dao.obtenerTodos();

        if (medicamentos.isEmpty()) {
            System.out.println("No se encontraron registros en la base de datos.");
            return;
        }

        System.out.println("=================================================");
        System.out.println("   CARESTOCK - ENVIADOR DE INFORMACIÓN (JAVA)    ");
        System.out.println("=================================================\n");

        System.out.println("--- FORMATO JSON ---");
        StringBuilder jsonArray = new StringBuilder("[\n");
        for (int i = 0; i < medicamentos.size(); i++) {
            jsonArray.append("  ").append(medicamentos.get(i).toJSON());
            if (i < medicamentos.size() - 1) jsonArray.append(",");
            jsonArray.append("\n");
        }
        jsonArray.append("]");
        System.out.println(jsonArray.toString());

        System.out.println("\n--- FORMATO CSV ---");
        System.out.println("ID,CODIGO_INVIMA,NOMBRE,PRINCIPIO_ACTIVO,CONCENTRACION,CATEGORIA,STOCK_TOTAL,STOCK_MINIMO");
        for (Medicamento m : medicamentos) {
            System.out.println(m.toCSV());
        }
    }
}
