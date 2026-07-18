package com.casadelosol.inventario;

import com.casadelosol.inventario.dao.*;
import com.casadelosol.inventario.model.*;

import java.time.LocalDate;

public class DataSeeder {

    public static void seed() {
        MateriaPrimaDAO mpDAO = new MateriaPrimaDAO();
        if (!mpDAO.findAll().isEmpty()) return;

        System.out.println("Sembrando datos de prueba...");

        CategoriaMPDAO catMPDAO = new CategoriaMPDAO();
        CategoriaPTDAO catPTDAO = new CategoriaPTDAO();
        ProductoTerminadoDAO ptDAO = new ProductoTerminadoDAO();
        CompraDAO compraDAO = new CompraDAO();
        RecetaDAO recetaDAO = new RecetaDAO();
        ProduccionDAO prodDAO = new ProduccionDAO();
        VentaDAO ventaDAO = new VentaDAO();

        // === CATEGORÍAS MP ===
        CategoriaMP aceites = new CategoriaMP("Aceites y Mantecas");
        catMPDAO.save(aceites);

        CategoriaMP polvos = new CategoriaMP("Polvos y Arcillas");
        catMPDAO.save(polvos);

        CategoriaMP ae = new CategoriaMP("Aceites Esenciales");
        catMPDAO.save(ae);

        CategoriaMP envases = new CategoriaMP("Envases y Empaques");
        catMPDAO.save(envases);

        CategoriaMP subAceites = new CategoriaMP("Aceites Vegetales");
        subAceites.setCategoriaPadreId(aceites.getId());
        catMPDAO.save(subAceites);

        // === CATEGORÍAS PT ===
        CategoriaPT shampoos = new CategoriaPT("Shampoos en Barra");
        catPTDAO.save(shampoos);

        CategoriaPT pastas = new CategoriaPT("Pasta Dental");
        catPTDAO.save(pastas);

        CategoriaPT desodorantes = new CategoriaPT("Desodorantes");
        catPTDAO.save(desodorantes);

        CategoriaPT jabones = new CategoriaPT("Jabones Corporales");
        catPTDAO.save(jabones);

        // === MATERIAS PRIMAS ===
        MateriaPrima karite = new MateriaPrima("Manteca de Karité", "gramos", aceites.getId());
        mpDAO.save(karite);

        MateriaPrima coco = new MateriaPrima("Aceite de Coco", "gramos", subAceites.getId());
        mpDAO.save(coco);

        MateriaPrima arcilla = new MateriaPrima("Arcilla Blanca (Caolín)", "gramos", polvos.getId());
        mpDAO.save(arcilla);

        MateriaPrima menta = new MateriaPrima("Aceite Esencial de Menta", "ml", ae.getId());
        mpDAO.save(menta);

        MateriaPrima lavanda = new MateriaPrima("Aceite Esencial de Lavanda", "ml", ae.getId());
        mpDAO.save(lavanda);

        MateriaPrima bicarbonato = new MateriaPrima("Bicarbonato de Sodio", "gramos", polvos.getId());
        mpDAO.save(bicarbonato);

        MateriaPrima glicerina = new MateriaPrima("Glicerina Vegetal", "ml", subAceites.getId());
        mpDAO.save(glicerina);

        MateriaPrima envasePet = new MateriaPrima("Envase PET 100ml", "unidades", envases.getId());
        mpDAO.save(envasePet);

        MateriaPrima sosa = new MateriaPrima("Hidróxido de Sodio (Sosa)", "gramos", polvos.getId());
        mpDAO.save(sosa);

        MateriaPrima mantecaCacao = new MateriaPrima("Manteca de Cacao", "gramos", aceites.getId());
        mpDAO.save(mantecaCacao);

        MateriaPrima xtian = new MateriaPrima("Aceite Esencial de Naranja", "ml", ae.getId());
        mpDAO.save(xtian);

        // === PRODUCTOS TERMINADOS ===
        ProductoTerminado shampooGraso = new ProductoTerminado("Shampoo en Barra - Cabello Graso", 320.0, shampoos.getId());
        ptDAO.save(shampooGraso);

        ProductoTerminado shampooSeco = new ProductoTerminado("Shampoo en Barra - Cabello Seco", 320.0, shampoos.getId());
        ptDAO.save(shampooSeco);

        ProductoTerminado pastaDental = new ProductoTerminado("Pasta Dental Natural - Menta", 250.0, pastas.getId());
        ptDAO.save(pastaDental);

        ProductoTerminado desodorante = new ProductoTerminado("Desodorante en Crema - Lavanda", 180.0, desodorantes.getId());
        ptDAO.save(desodorante);

        ProductoTerminado jabonAvena = new ProductoTerminado("Jabón Corporal - Avena y Miel", 150.0, jabones.getId());
        ptDAO.save(jabonAvena);

        // === RECETAS ===
        Receta rShampooGraso = new Receta(shampooGraso.getId());
        rShampooGraso.setNotas("Mezclar mantecas derretidas a baño María. Agregar aceites esenciales cuando la mezcla esté a 40°C. Verter en molde y dejar reposar 24hs.");
        rShampooGraso.getDetalles().add(new RecetaDetalle(karite.getId(), 50));
        rShampooGraso.getDetalles().add(new RecetaDetalle(coco.getId(), 30));
        rShampooGraso.getDetalles().add(new RecetaDetalle(arcilla.getId(), 15));
        rShampooGraso.getDetalles().add(new RecetaDetalle(menta.getId(), 3));
        rShampooGraso.getDetalles().add(new RecetaDetalle(sosa.getId(), 10));
        recetaDAO.save(rShampooGraso);

        Receta rShampooSeco = new Receta(shampooSeco.getId());
        rShampooSeco.setNotas("Misma técnica que el shampoo graso pero reemplazar arcilla por manteca de cacao para mayor hidratación.");
        rShampooSeco.getDetalles().add(new RecetaDetalle(karite.getId(), 40));
        rShampooSeco.getDetalles().add(new RecetaDetalle(coco.getId(), 25));
        rShampooSeco.getDetalles().add(new RecetaDetalle(mantecaCacao.getId(), 20));
        rShampooSeco.getDetalles().add(new RecetaDetalle(lavanda.getId(), 3));
        rShampooSeco.getDetalles().add(new RecetaDetalle(sosa.getId(), 10));
        recetaDAO.save(rShampooSeco);

        Receta rPastaDental = new Receta(pastaDental.getId());
        rPastaDental.setNotas("Mezclar polvos primero. Agregar glicerina de a poco hasta formar pasta. Añadir menta al final.");
        rPastaDental.getDetalles().add(new RecetaDetalle(bicarbonato.getId(), 30));
        rPastaDental.getDetalles().add(new RecetaDetalle(arcilla.getId(), 20));
        rPastaDental.getDetalles().add(new RecetaDetalle(glicerina.getId(), 15));
        rPastaDental.getDetalles().add(new RecetaDetalle(menta.getId(), 2));
        recetaDAO.save(rPastaDental);

        Receta rDesodorante = new Receta(desodorante.getId());
        rDesodorante.setNotas("Mezclar bicarbonato con manteca de coco derretida. Agregar lavanda y envasar en caliente.");
        rDesodorante.getDetalles().add(new RecetaDetalle(bicarbonato.getId(), 25));
        rDesodorante.getDetalles().add(new RecetaDetalle(coco.getId(), 40));
        rDesodorante.getDetalles().add(new RecetaDetalle(lavanda.getId(), 2));
        recetaDAO.save(rDesodorante);

        Receta rJabon = new Receta(jabonAvena.getId());
        rJabon.setNotas("Saponificación en frío. Curar 4 semanas antes de usar.");
        rJabon.getDetalles().add(new RecetaDetalle(coco.getId(), 60));
        rJabon.getDetalles().add(new RecetaDetalle(karite.getId(), 30));
        rJabon.getDetalles().add(new RecetaDetalle(sosa.getId(), 15));
        rJabon.getDetalles().add(new RecetaDetalle(xtian.getId(), 2));
        recetaDAO.save(rJabon);

        // === COMPRAS (auto-actualizan stock MP) ===
        compraDAO.save(compra(karite.getId(), LocalDate.of(2026, 6, 1), 2000, 18000.0, "Distribuidora Natural S.A."));
        compraDAO.save(compra(coco.getId(), LocalDate.of(2026, 6, 5), 3000, 21000.0, "Aceites del Sur"));
        compraDAO.save(compra(arcilla.getId(), LocalDate.of(2026, 6, 8), 1500, 7500.0, "Minerales Argentinos"));
        compraDAO.save(compra(menta.getId(), LocalDate.of(2026, 6, 10), 200, 14000.0, "Aromas Naturales"));
        compraDAO.save(compra(lavanda.getId(), LocalDate.of(2026, 6, 10), 200, 16000.0, "Aromas Naturales"));
        compraDAO.save(compra(bicarbonato.getId(), LocalDate.of(2026, 6, 12), 5000, 10000.0, "Droguería Buenos Aires"));
        compraDAO.save(compra(glicerina.getId(), LocalDate.of(2026, 6, 15), 1000, 8500.0, "Distribuidora Natural S.A."));
        compraDAO.save(compra(sosa.getId(), LocalDate.of(2026, 6, 18), 2000, 14000.0, "Droguería Buenos Aires"));
        compraDAO.save(compra(mantecaCacao.getId(), LocalDate.of(2026, 6, 20), 1500, 19500.0, "Distribuidora Natural S.A."));
        compraDAO.save(compra(xtian.getId(), LocalDate.of(2026, 7, 1), 100, 9000.0, "Aromas Naturales"));

        // === PRODUCCIONES (descuentan MP, suman PT) ===
        prodDAO.save(new Produccion(shampooGraso.getId(), 15, LocalDate.of(2026, 6, 20)));
        prodDAO.save(new Produccion(shampooSeco.getId(), 10, LocalDate.of(2026, 6, 22)));
        prodDAO.save(new Produccion(pastaDental.getId(), 30, LocalDate.of(2026, 6, 25)));
        prodDAO.save(new Produccion(desodorante.getId(), 25, LocalDate.of(2026, 7, 1)));
        prodDAO.save(new Produccion(jabonAvena.getId(), 20, LocalDate.of(2026, 7, 5)));

        // === VENTAS (descuentan PT) ===
        ventaDAO.save(new Venta(shampooGraso.getId(), 3, LocalDate.of(2026, 6, 25)));
        ventaDAO.save(new Venta(pastaDental.getId(), 8, LocalDate.of(2026, 6, 28)));
        ventaDAO.save(new Venta(desodorante.getId(), 5, LocalDate.of(2026, 7, 3)));
        ventaDAO.save(new Venta(shampooGraso.getId(), 2, LocalDate.of(2026, 7, 5)));
        ventaDAO.save(new Venta(jabonAvena.getId(), 6, LocalDate.of(2026, 7, 6)));
        ventaDAO.save(new Venta(pastaDental.getId(), 4, LocalDate.of(2026, 7, 8)));

        System.out.println("Seed completado: datos de prueba insertados.");
    }

    private static Compra compra(int mpId, LocalDate fecha, double cantidad, double precio, String lugar) {
        Compra c = new Compra(mpId, fecha, cantidad, precio);
        c.setLugar(lugar);
        return c;
    }
}
