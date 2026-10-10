module org.example.carestock {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires javafx.media;
    requires java.sql;
    requires jbcrypt;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.carestock to javafx.fxml;
    opens org.example.carestock.model to javafx.fxml, javafx.base;
    opens org.example.carestock.controller to javafx.fxml;

    exports org.example.carestock;
    exports org.example.carestock.model;
    exports org.example.carestock.exception;
    exports org.example.carestock.config;
    exports org.example.carestock.dao;
    exports org.example.carestock.service;
    exports org.example.carestock.controller;
    exports org.example.carestock.facade;
    exports org.example.carestock.DataTransferObject;
    exports org.example.carestock.session;
}
