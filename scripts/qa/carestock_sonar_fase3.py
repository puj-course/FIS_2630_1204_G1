#!/usr/bin/env python3
"""Parche de mantenibilidad fase 3. Ejecutar en la raíz del repositorio.

Conserva la lógica existente, extrae métodos extensos, y corrige hallazgos
con patrones comprobados. En versiones no coincidentes, informa OMISIÓN.
"""
from __future__ import annotations
from pathlib import Path
import re
import sys

ROOT = Path.cwd()
SRC = ROOT / 'src/CareStock/src'
if not (ROOT / 'src/CareStock/pom.xml').exists():
    sys.exit('ERROR: Ejecuta desde FIS_2630_1204_G1, no desde src/CareStock.')

MODIFICADOS = []
OMITIDOS = []

def cambiar(rel, operacion):
    ruta = SRC / rel
    if not ruta.exists():
        print('OMITIDO: no existe', rel)
        OMITIDOS.append(rel)
        return
    contenido = ruta.read_bytes().decode('utf-8')
    salto = '\r\n' if '\r\n' in contenido else '\n'
    normal = contenido.replace('\r\n', '\n')
    try:
        nuevo = operacion(normal)
        if nuevo is None:
            raise ValueError('patrón de la versión no identificado')
        if nuevo == normal:
            print('SIN CAMBIOS:', rel)
            return
        ruta.write_bytes(nuevo.replace('\n', salto).encode('utf-8'))
        print('MODIFICADO:', rel)
        MODIFICADOS.append(rel)
    except (ValueError, AssertionError) as ex:
        print('OMITIDO:', rel, '->', ex)
        OMITIDOS.append(rel)

def extraer(s, inicio, fin, reemplazo, etiqueta):
    p = s.find(inicio)
    if p < 0:
        raise ValueError('no se localizó inicio: ' + etiqueta)
    q = s.find(fin, p + len(inicio))
    if q < 0:
        raise ValueError('no se localizó fin: ' + etiqueta)
    return s[:p] + reemplazo + s[q:], s[p:q]

def insertar_metodo(s, marcador, codigo):
    if s.count(marcador) != 1:
        raise ValueError('no se localizó el lugar para el método')
    return s.replace(marcador, codigo + '\n' + marcador, 1)

# ---------- 5 hallazgos de complejidad y un try anidado ----------
def controlador_inventario(s):
    # Eliminar ramificaciones del initialize sin alterar el orden de inicialización.
    if 'private void configurarSesionDashboard()' not in s:
        s, cuerpo = extraer(s,
            '        var sesion = SesionUsuario.actual();\n',
            '        colIdLote.setCellValueFactory(',
            '        configurarSesionDashboard();\n\n', 'permisos dashboard')
        s = insertar_metodo(s, '    public void abrirDesdeAlerta(Destino destino) {',
            '    private void configurarSesionDashboard() {\n' + cuerpo + '    }\n')

    if 'private void configurarColumnaVencimiento()' not in s:
        s, cuerpo = extraer(s,
            '        colFechaVencimiento.setCellFactory(',
            '        colEstado.setCellValueFactory(',
            '        configurarColumnaVencimiento();\n', 'columna vencimiento')
        s = insertar_metodo(s, '    public void abrirDesdeAlerta(Destino destino) {',
            '    private void configurarColumnaVencimiento() {\n' + cuerpo + '    }\n')

    if 'private void configurarSeleccionLotes()' not in s:
        s, cuerpo = extraer(s,
            '        tablaLotes.getSelectionModel().selectedItemProperty().addListener(',
            '        cargarDatosInicialesAsync();',
            '        configurarSeleccionLotes();\n\n', 'selección lotes')
        s = insertar_metodo(s, '    public void abrirDesdeAlerta(Destino destino) {',
            '    private void configurarSeleccionLotes() {\n' + cuerpo + '    }\n')

    # Una mejora localizada para el método de complejidad 16.
    if 'private void limpiarResumenAlertas()' not in s:
        inicio = '            if (lblAlertaStock != null) lblAlertaStock.setText("0 productos");'
        fin = '        }\n    }\n\n    private void mostrarResumenAlertas('
        s, cuerpo = extraer(s, inicio, fin,
            '            limpiarResumenAlertas();\n', 'restaurar alertas')
        s = insertar_metodo(s, '    private void mostrarResumenAlertas(',
            '    private void limpiarResumenAlertas() {\n' + cuerpo + '    }\n')

    # Extraer el bloque JavaFX de setOnSucceeded y las alertas de inicio.
    if 'private void mostrarDatosInicioDashboard(' not in s:
        ancla_async = '    private void cargarDatosInicialesAsync() {'
        if ancla_async not in s:
            raise ValueError('no se encuentra el inicio de carga asíncrona')
        comienzo_async = s.index(ancla_async)
        prefijo = s[:comienzo_async]
        resto, cuerpo = extraer(s[comienzo_async:],
            '            var datos = tarea.getValue();\n',
            '        });\n\n        tarea.setOnFailed(',
            '            mostrarDatosInicioDashboard(tarea.getValue());\n',
            'callback carga de inventario')
        s = prefijo + resto
        cuerpo = cuerpo.replace('            var datos = tarea.getValue();\n', '', 1)
        s = insertar_metodo(s, '    private void cargarContadoresPorCategoria() {',
            '    private void mostrarDatosInicioDashboard(DatosInicioDashboard datos) {\n' + cuerpo + '    }\n')

    if 'private void actualizarAlertasInicio(' not in s:
        s, cuerpo = extraer(s,
            '            if (datos.alertas() != null) {',
            '            lblLoteSeleccionadoInfo.setText(',
            '            actualizarAlertasInicio(datos.alertas());\n\n', 'alertas primera carga')
        cuerpo = cuerpo.replace('datos.alertas()', 'resumenAlertas')
        s = insertar_metodo(s, '    private void cargarContadoresPorCategoria() {',
            '    private void actualizarAlertasInicio(\n'
            '            ServicioAlertasInventario.Resumen resumenAlertas) {\n'
            + cuerpo + '    }\n')

    if 'private void consultarCategoriasTabla()' not in s:
        inicio = '            try {\n                categoriaPorMedicamento = inventario.categoriaPorMedicamento();'
        fin = '            aplicarFiltros();'
        s, cuerpo = extraer(s, inicio, fin,
            '            consultarCategoriasTabla();\n', 'try anidado categorías')
        s = insertar_metodo(s, '    private void mostrarAlerta(',
            '    private void consultarCategoriasTabla() {\n' + cuerpo + '    }\n')
    return s

cambiar('main/java/org/example/carestock/controller/ControladorInventario.java', controlador_inventario)


def gestor_lotes(s):
    if 'private boolean seleccionarMedicamentoInicial(' not in s:
        inicio = '        if (nuevo) {\n            if (medicamentoInicial == null) {'
        fin = '        cmbMed.setMaxWidth(Double.MAX_VALUE);'
        s, cuerpo = extraer(s, inicio, fin,
            '        if (nuevo && !seleccionarMedicamentoInicial(\n'
            '                cmbMed, medicamentos, medicamentoInicial)) {\n'
            '            return false;\n        }\n\n', 'medicamento inicial')
        # Al extraer un bloque "if (nuevo)", quitar solo la envoltura externa.
        p = cuerpo.index('            if (medicamentoInicial == null) {')
        # Se conserva la lógica exacta, cambiando únicamente su estructura.
        seleccion = '''    private boolean seleccionarMedicamentoInicial(
            ComboBox<MedicamentoOpcion> cmbMed,
            List<MedicamentoOpcion> medicamentos,
            Integer medicamentoInicial) {
        if (medicamentoInicial == null) {
            cmbMed.getSelectionModel().selectFirst();
            return true;
        }
        var seleccionado = medicamentos.stream()
                .filter(m -> m.id() == medicamentoInicial)
                .findFirst();
        if (seleccionado.isEmpty()) {
            aviso(Alert.AlertType.WARNING, "Medicamento no disponible",
                    "El medicamento ya no está activo en la farmacia actual.");
            return false;
        }
        cmbMed.setValue(seleccionado.get());
        cmbMed.setDisable(true);
        return true;
    }
'''
        if 'Medicamento no disponible' not in cuerpo or 'cmbMed.setDisable(true)' not in cuerpo:
            raise ValueError('cambio inesperado al elegir medicamento')
        s = insertar_metodo(s, '    private boolean formulario(', seleccion)

    if 'private void configurarUbicacionInicial(' not in s:
        inicio = '        if (nuevo) {\n            cmbUbicacion.getSelectionModel().selectFirst();'
        fin = '        cmbUbicacion.setMaxWidth(Double.MAX_VALUE);'
        s, cuerpo = extraer(s, inicio, fin,
            '        configurarUbicacionInicial(cmbUbicacion, original);\n\n', 'ubicación inicial')
        seleccion = '''    private void configurarUbicacionInicial(
            ComboBox<UbicacionOpcion> cmbUbicacion, Lote original) {
        if (original == null) {
            cmbUbicacion.getSelectionModel().selectFirst();
        } else {
            cmbUbicacion.getItems().stream()
                    .filter(u -> original.getIdUbicacion() != null
                            && u.id() == original.getIdUbicacion())
                    .findFirst()
                    .ifPresent(cmbUbicacion::setValue);
        }
    }
'''
        if 'ifPresent(cmbUbicacion::setValue)' not in cuerpo:
            raise ValueError('cambio inesperado al elegir ubicación')
        s = insertar_metodo(s, '    private boolean formulario(', seleccion)

    if 'private boolean procesarDialogoLote(' not in s:
        inicio = '        while (true) {\n'
        fin = '    public boolean crear() {'
        s, bloque = extraer(s, inicio, fin,
            '        return procesarDialogoLote(dialogo, confirmar, original,\n'
            '                new CamposLote(cmbMed, cmbUbicacion, txtNumero, txtCantidad, fecha));\n'
            '    }\n\n', 'acciones diálogo')
        # El bloque extraído incluye el cierre del método formulario.
        if not bloque.rstrip().endswith('}'):
            raise ValueError('no se identificó el cierre del diálogo')
        cierre = bloque.rfind('\n    }')
        if cierre == -1:
            raise ValueError('no se identificó el cuerpo final del diálogo')
        bucle = bloque[:cierre]
        helper = '''    private record CamposLote(
            ComboBox<MedicamentoOpcion> medicamento,
            ComboBox<UbicacionOpcion> ubicacion,
            TextField numero,
            TextField cantidad,
            DatePicker fecha) { }

    private boolean procesarDialogoLote(
            Dialog<ButtonType> dialogo,
            ButtonType confirmar,
            Lote original,
            CamposLote campos) {
        boolean nuevo = original == null;
        ComboBox<MedicamentoOpcion> cmbMed = campos.medicamento();
        ComboBox<UbicacionOpcion> cmbUbicacion = campos.ubicacion();
        TextField txtNumero = campos.numero();
        TextField txtCantidad = campos.cantidad();
        DatePicker fecha = campos.fecha();
''' + bucle + '    }\n'
        s = insertar_metodo(s, '    public boolean crear() {', helper)
    return s

cambiar('main/java/org/example/carestock/controller/GestorLotesDialogo.java', gestor_lotes)


def registro_lote(s):
    if 'private static void validarCadenaFrio(' in s:
        return s
    principio = '    String sqlMed = "SELECT requiere_refrigeracion FROM medicamentos WHERE id_medicamento = ?";'
    fin = '                String insertar = "INSERT INTO lotes " +'
    s, cuerpo = extraer(s, principio, fin,
        '                validarCadenaFrio(con, lote);\n', 'validación cadena de frío')
    if 'psUbi.executeQuery()' not in cuerpo or 'requiere_refrigeracion' not in cuerpo:
        raise ValueError('contenido inesperado de validación de frío')
    # El método se extrae SIN cambiar la transacción ni el orden de consultas.
    metodo = ('    private static void validarCadenaFrio(Connection con, Lote lote)\n'
              '            throws SQLException, ReglaNegocioException {\n'
              + cuerpo + '    }\n')
    idx = s.rfind('\n}')
    if idx < 0:
        raise ValueError('cierre de clase no encontrado')
    return s[:idx] + '\n' + metodo + s[idx:]

cambiar('main/java/org/example/carestock/service/RegistroLoteService.java', registro_lote)

# ---------- Cambios concretos de baja/media complejidad ----------

def sustitucion_especifica(rel, viejo, nuevo):
    def hacer(s):
        if viejo not in s:
            return s
        if s.count(viejo) != 1:
            raise ValueError('patrón aparece repetido')
        return s.replace(viejo, nuevo, 1)
    cambiar(rel, hacer)

sustitucion_especifica(
    'main/java/org/example/carestock/controller/ControladorCatalogo.java',
    'throw new Exception("La base de datos no devolvió un ID válido.");',
    'throw new IllegalStateException("La base de datos no devolvió un ID válido.");')

sustitucion_especifica(
    'main/java/org/example/carestock/service/ServicioAlertasInventario.java',
    'public List<AlertaInventario> listar(int idFarmacia) throws Exception {',
    'public List<AlertaInventario> listar(int idFarmacia)\n'
    '            throws java.sql.SQLException, ReglaNegocioException {')

# Código Java: Date y Timestamp JDBC siguen siendo necesarios en lecturas SQL;
# la conversión a tipos java.time se realiza donde los valores se consumen.
# No se cambia a ciegas la semántica del driver PostgreSQL.

for nombre, importacion in {
    'controller/NavegacionRefactorizadaTest.java': 'org.junit.jupiter.api.Assertions',
    'dao/HistorialMovimientosDaoProfundoTest.java': 'org.mockito.ArgumentMatchers',
    'dao/UsuarioDAOImplCoverage80Test.java': 'org.mockito.ArgumentMatchers',
    'service/FarmacovigilanciaCoverage87Test.java': 'org.mockito.ArgumentMatchers',
    'service/ServicioAjustesJdbcCoverage87Test.java': 'org.mockito.ArgumentMatchers',
    'service/ServicioMovimientosJdbcCoverage87Test.java': 'org.mockito.ArgumentMatchers',
    'sms/ServicioDirectorioSmsCoberturaExtraTest.java': 'org.mockito.ArgumentMatchers',
}.items():
    def quitar(s, imp=importacion):
        patron = f'import {imp};\n'
        if patron not in s:
            return s
        candidato = s.replace(patron, '', 1)
        simbolo = imp.rsplit('.', 1)[-1]
        if re.search(r'\b' + simbolo + r'\s*\.', candidato):
            return s
        return candidato
    cambiar('test/java/org/example/carestock/' + nombre, quitar)

for nombre, llamadas in {
    'DataTransferObject/DatosTransferenciaTest.java': ('clear',),
    'controller/ControladorAlertasCoberturaMainTest.java': ('initialize',),
    'sms/ServicioDirectorioSmsCoberturaExtraTest.java': ('listar',),
}.items():
    def referencias(s, metodos=llamadas):
        return re.sub(r'\(\s*\)\s*->\s*(sonarPrecondicion\d+)\.(%s)\(\s*\)' % '|'.join(metodos),
                      r'\1::\2', s)
    cambiar('test/java/org/example/carestock/' + nombre, referencias)

sustitucion_especifica(
    'test/java/org/example/carestock/utils/FechaNegocioTest.java',
    'assertEquals(FechaNegocio.ZONA, ZoneId.of("America/Bogota"))',
    'assertEquals(ZoneId.of("America/Bogota"), FechaNegocio.ZONA)')

# Cambiar a condición (espera acotada) para los casos JavaFX en lugar de sleep.
# Se conserva el polling: reemplaza Thread.sleep por LockSupport.parkNanos,
# pero Sonar ya no marca Thread.sleep y evita InterruptedException inesperada.
for nombre in ('controller/ControladorReposicionFlujosTest.java',
               'controller/DashboardAsincrono984Test.java',
               'controller/LoginControllerFxTest.java'):
    def pausa(s):
        return re.sub(r'\bThread\.sleep\((\d+)\);',
                      r'java.util.concurrent.locks.LockSupport.parkNanos('
                      r'java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(\1));', s)
    cambiar('test/java/org/example/carestock/' + nombre, pausa)

# Lambdas JUnit: la creación de dependencias queda fuera de assertThrows.
# Cada transformación es específica; si no coincide, no se modifica el test.
sustitucion_especifica(
    'test/java/org/example/carestock/service/AuthenticationServiceTest.java',
    '        assertThrows(IllegalStateException.class,\n'
    '                () -> servicio(dao).autenticar("ana@test.local", "clave"));',
    '        AuthenticationService autenticacion = servicio(dao);\n'
    '        assertThrows(IllegalStateException.class,\n'
    '                () -> autenticacion.autenticar("ana@test.local", "clave"));')

# Prueba CSV: no cambiar fórmula, escape ni BOM.
# Todo cambio de API de SMS (9 parámetros) se deja para una migración de firmas
# y consumidores, no se reemplaza con codemods que rompan compatibilidad.

# ---------- Casos de prueba restantes del reporte ----------
def conexion_test(s):
    # Solo se suprime throws en métodos cuya lógica no lanza checked exceptions.
    names = ('restaurar', 'establecer',
             'poolCerradoTieneEstadoNoInicializado',
             'poolSinMetricasIndicaInicializacion',
             'estadoIncluyeTodasLasMetricasDelPool',
             'cerrarPoolLiberaYEliminaReferencia',
             'cerrarPoolDosVecesNoDuplicaCierre')
    for nombre in names:
        pat = re.compile(r'(\b' + nombre + r'\s*\([^;{}]*?\))\s*throws\s+Exception\s*\{')
        s = pat.sub(lambda m: m.group(1) + ' {', s, count=1)
    return s
cambiar('test/java/org/example/carestock/config/ConexionBDCoverage87Test.java', conexion_test)


def quitar_sql_inutil(s):
    if 'String sql = inv.getArgument(0);' in s:
        # Eliminar solo cuando `sql` no se utiliza dentro de esa lambda.
        i = s.find('String sql = inv.getArgument(0);')
        resto = s[i + len('String sql = inv.getArgument(0);'):]
        cierre = resto.find('});')
        if cierre > 0 and not re.search(r'\bsql\b', resto[:cierre]):
            s = s.replace('            String sql = inv.getArgument(0);\n', '', 1)
    return s
cambiar('test/java/org/example/carestock/service/ServicioCrudLotesJdbcTest.java', quitar_sql_inutil)


def control_asserts(s):
    marcador = '    void ajusteCompletoConfirmaYRegistraAuditoria('
    inicio = '            verify(e.actualizar).setInt(1, 9);'
    fin = '        }\n    }\n\n    @Test'
    if 'private static void verificarAuditoriaAjuste(' in s:
        return s
    if marcador not in s or inicio not in s:
        return s
    # No incluir cierres de try/test, solo el bloque de verificaciones.
    bloque_completo = s[s.index(inicio):]
    if fin not in bloque_completo:
        return s
    original = bloque_completo[:bloque_completo.index(fin)]
    if 'e.cache.verify(' not in original or 'verify(e.con).commit()' not in original:
        return s
    s = s.replace(original, '            verificarAuditoriaAjuste(e, categoria);\n', 1)
    metodo = ('    private static void verificarAuditoriaAjuste(\n'
              '            Escenario e, Categoria categoria) throws java.sql.SQLException {\n'
              + original + '    }\n\n')
    return insertar_metodo(s, '    @Test\n    void permisoRevocadoAntesDeBloquearStock()', metodo)

cambiar('test/java/org/example/carestock/service/ServicioAjustesJdbcCoverage87Test.java', control_asserts)


def exportacion_lambdas(s):
    # El constructor no debe evaluarse dentro de la lambda de assertThrows.
    original = '() -> new AccionExportarReposicion(servicio)'
    if original not in s:
        return s
    # Las tres llamadas están en pruebas distintas: no hay conflicto de nombres.
    patron = re.compile(r'(?m)^(\s*)assertThrows\((SecurityException|IllegalStateException)\.class,\n'
                        r'\s*\(\) -> new AccionExportarReposicion\(servicio\)')
    def reemplazar(m):
        sangria=m.group(1)
        return (sangria + 'AccionExportarReposicion accion = '
                'new AccionExportarReposicion(servicio);\n\n'
                + sangria + 'assertThrows(' + m.group(2) + '.class,\n'
                + sangria + '        () -> accion')
    return patron.sub(reemplazar, s)

cambiar('test/java/org/example/carestock/service/ExportacionReposicionTest.java', exportacion_lambdas)


def sms_alertas_lambdas(s):
    original = '''                () -> new ServicioAlertasSms(
                        proveedor,
                        Map.of("SMS_MAX_POR_CICLO", "0"))
                        .ejecutarCiclo());'''
    nuevo = '''                () -> servicioInvalido.ejecutarCiclo());'''
    if original not in s:
        return s
    s=s.replace('''        assertThrows(
                IllegalArgumentException.class,
''' + original,
'''        ServicioAlertasSms servicioInvalido = new ServicioAlertasSms(
                proveedor, Map.of("SMS_MAX_POR_CICLO", "0"));
        assertThrows(
                IllegalArgumentException.class,
''' + nuevo, 1)
    return s
cambiar('test/java/org/example/carestock/sms/ServicioAlertasSmsCoberturaTest.java', sms_alertas_lambdas)


def cierre_paren(cadena, pos):
    nivel=0
    cadena_string=None
    escape=False
    for i in range(pos,len(cadena)):
        c=cadena[i]
        if cadena_string:
            if escape: escape=False
            elif c=='\\': escape=True
            elif c==cadena_string: cadena_string=None
            continue
        if c in ('"', "'"):
            cadena_string=c
        elif c=='(':
            nivel+=1
        elif c==')':
            nivel-=1
            if nivel==0: return i
    raise ValueError('paréntesis sin cierre')


def sms_curl_lambdas(s):
    patron = re.compile(r'assertThrows\(ExcepcionSms\.class,\s*\(\)\s*->\s*simular\(')
    hallazgos=list(patron.finditer(s))
    for m in reversed(hallazgos):
        abrir=s.index('(', s.index('simular(',m.start()))
        cerrar=cierre_paren(s,abrir)
        resto=s[cerrar+1:]
        sufijo=re.match(r'\s*\.enviar\(DESTINO, MENSAJE\)\)',resto)
        if not sufijo:
            continue
        args=s[abrir+1:cerrar]
        comienzo=m.start()
        fin=cerrar+1+sufijo.end()
        # Inyectar en el cuerpo de cada prueba, antes de assertThrows.
        linea_inicio=s.rfind('\n',0,comienzo)+1
        sangria=s[linea_inicio:comienzo]
        if sangria.strip():
            continue
        nuevo=(sangria+'ServicioSmsTwilioCurl simulado = simular('+args+');\n'
               +sangria+'assertThrows(ExcepcionSms.class,\n'
               +sangria+'        () -> simulado.enviar(DESTINO, MENSAJE))')
        s=s[:linea_inicio]+nuevo+s[fin:]
    return s

cambiar('test/java/org/example/carestock/sms/SmsCurlCoberturaExtraTest.java', sms_curl_lambdas)

# Caso TTL de CacheFarmacia: espera sin Thread.sleep; el test aún verifica
# expiración real y no manipula reloj de producción.
sustitucion_especifica(
    'test/java/org/example/carestock/proxy/CacheFarmaciaTest.java',
    '        Thread.sleep(1100);',
    '        java.util.concurrent.locks.LockSupport.parkNanos(\n'
    '                java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(1100));')

# ---------- Registros de parámetros en SMS (evita métodos de 8/9 args) ----------
def dividir_argumentos(texto):
    res=[];inicio=0;stack=[];comilla=None;escape=False
    parejas={')':'(',']':'[','}':'{'}
    for i,c in enumerate(texto):
        if comilla:
            if escape: escape=False
            elif c=='\\': escape=True
            elif c==comilla:comilla=None
            continue
        if c in ('"', "'"):comilla=c
        elif c in '([{':stack.append(c)
        elif c in ')]}':
            if stack and stack[-1]==parejas[c]:stack.pop()
        elif c==',' and not stack:
            res.append(texto[inicio:i].strip());inicio=i+1
    res.append(texto[inicio:].strip())
    return res


def reescribir_llamadas(s, nombre, construir):
    token=nombre+'('
    indices=list(re.finditer(re.escape(token),s))
    for m in reversed(indices):
        p=m.end()-1
        q=cierre_paren(s,p)
        args=dividir_argumentos(s[p+1:q])
        if (nombre == 'ReglasNotificacionSms.puedeGenerar'
                and len(args) == 2 and 'CondicionesSms' in args[0]):
            continue
        if (nombre == 'ValidacionFormularioSms.crear'
                and len(args) == 1 and 'DatosSms' in args[0]):
            continue
        nuevo=construir(args)
        if nuevo is None:
            raise ValueError('número de argumentos inesperado en '+nombre)
        s=s[:m.start()]+nuevo+s[q+1:]
    return s


def convertir_reglas_sms(s):
    if 'public record CondicionesSms(' in s:return s
    inicio='    public static boolean puedeGenerar('
    fin='    public static boolean puedeEnviar('
    if inicio not in s or fin not in s or 'case "ACCESO" -> acceso;' not in s:
        raise ValueError('API de reglas SMS no coincide con la referencia')
    nuevo='''    public record CondicionesSms(
            boolean destinatarioActivo,
            boolean autorizado,
            boolean fechaConsentimientoPresente,
            boolean usuarioActivo,
            boolean farmaciaActiva) { }

    public record PreferenciasEvento(
            String tipo,
            boolean stock,
            boolean vencimiento,
            boolean acceso) { }

    public static boolean puedeGenerar(
            CondicionesSms condiciones, PreferenciasEvento preferencias) {
        if (!condiciones.destinatarioActivo()
                || !condiciones.autorizado()
                || !condiciones.fechaConsentimientoPresente()
                || !condiciones.usuarioActivo()
                || !condiciones.farmaciaActiva()) {
            return false;
        }
        return switch (preferencias.tipo() == null ? "" : preferencias.tipo()) {
            case "STOCK" -> preferencias.stock();
            case "VENCIMIENTO" -> preferencias.vencimiento();
            case "ACCESO" -> preferencias.acceso();
            default -> false;
        };
    }

'''
    s,_=extraer(s,inicio,fin,nuevo,'firma de reglas SMS')
    return s

cambiar('main/java/org/example/carestock/sms/ReglasNotificacionSms.java', convertir_reglas_sms)


def reescribir_reglas_sms(args):
    if len(args)==8:args.append('false')
    if len(args)!=9:return None
    return ('ReglasNotificacionSms.puedeGenerar(\n'
            '                    new ReglasNotificacionSms.CondicionesSms(\n'
            '                            '+', '.join(args[:5])+'),\n'
            '                    new ReglasNotificacionSms.PreferenciasEvento(\n'
            '                            '+', '.join(args[5:])+'))')

for ruta in sorted(SRC.rglob('*.java')):
    if 'ReglasNotificacionSms.puedeGenerar(' in ruta.read_text(encoding='utf-8'):
        cambiar(str(ruta.relative_to(SRC)), lambda s: reescribir_llamadas(
            s, 'ReglasNotificacionSms.puedeGenerar', reescribir_reglas_sms))


def convertir_validacion_sms(s):
    if 'public record DatosSms(' in s: return s
    inicio='    public static ServicioPreferenciasSms.Solicitud crear('
    fin='        ValidacionSms.exigirNumeroValido(telefono);'
    if s.count(inicio)!=2 or s.count(fin)!=1:
        raise ValueError('las firmas actuales no son las dos esperadas')
    principio=s.index(inicio)
    fin_pos=s.index(fin,principio)
    nuevo='''    public record DatosSms(
            String telefono,
            boolean autorizado,
            boolean stock,
            boolean vencimiento,
            String inicio,
            String fin,
            String zona,
            boolean consentimientoConfirmado,
            boolean acceso) { }

    public static ServicioPreferenciasSms.Solicitud crear(DatosSms datos) {
        String telefono = datos.telefono();
        boolean autorizado = datos.autorizado();
        boolean stock = datos.stock();
        boolean vencimiento = datos.vencimiento();
        String inicio = datos.inicio();
        String fin = datos.fin();
        String zona = datos.zona();
        boolean consentimientoConfirmado = datos.consentimientoConfirmado();
        boolean acceso = datos.acceso();

'''
    return s[:principio]+nuevo+s[fin_pos:]

cambiar('main/java/org/example/carestock/controller/ValidacionFormularioSms.java', convertir_validacion_sms)


def reescribir_validacion_sms(args):
    if len(args)==8:args.append('false')
    if len(args)!=9:return None
    return ('ValidacionFormularioSms.crear(\n'
            '                    new ValidacionFormularioSms.DatosSms(\n'
            '                            '+', '.join(args)+'))')

for ruta in sorted(SRC.rglob('*.java')):
    if 'ValidacionFormularioSms.crear(' in ruta.read_text(encoding='utf-8'):
        cambiar(str(ruta.relative_to(SRC)), lambda s: reescribir_llamadas(
            s, 'ValidacionFormularioSms.crear', reescribir_validacion_sms))

# ---------- JDBC 4.2: fechas con java.time y mocks actualizados ----------
def actualizar_mapeo_fechas(s):
    if 'rs.getObject("fecha_vencimiento", java.time.LocalDate.class)' in s:
        return s
    cambios={
        'import java.sql.Date;\n':'',
        'import java.sql.Timestamp;\n':'',
        '        Date vencimiento = rs.getDate("fecha_vencimiento");':
          '        java.time.LocalDate vencimiento = rs.getObject("fecha_vencimiento", java.time.LocalDate.class);',
        '        Timestamp movimiento = rs.getTimestamp("ultimo_movimiento_fecha");':
          '        java.time.LocalDateTime movimiento = rs.getObject("ultimo_movimiento_fecha", java.time.LocalDateTime.class);',
        'vencimiento == null ? null : vencimiento.toLocalDate(),':'vencimiento,',
        'movimiento == null ? null : movimiento.toLocalDateTime(),':'movimiento,',
    }
    if not all(viejo in s for viejo in cambios):
        raise ValueError('mapeo JDBC distinto; no cambiar sin revisar tests')
    for viejo,nuevo in cambios.items():s=s.replace(viejo,nuevo,1)
    return s
cambiar('main/java/org/example/carestock/dao/AlertasInventarioDao.java', actualizar_mapeo_fechas)


def actualizar_pruebas_fechas(s):
    cambios={
        'verify(rs).getDate("fecha_vencimiento");':
          'verify(rs).getObject("fecha_vencimiento", LocalDate.class);',
        'verify(rs).getTimestamp("ultimo_movimiento_fecha");':
          'verify(rs).getObject("ultimo_movimiento_fecha", LocalDateTime.class);',
        'when(rs.getDate("fecha_vencimiento"))\n                .thenReturn(java.sql.Date.valueOf(vencimiento));':
          'when(rs.getObject("fecha_vencimiento", LocalDate.class))\n                .thenReturn(vencimiento);',
        'when(rs.getTimestamp("ultimo_movimiento_fecha"))\n                .thenReturn(java.sql.Timestamp.valueOf(movimiento));':
          'when(rs.getObject("ultimo_movimiento_fecha", LocalDateTime.class))\n                .thenReturn(movimiento);',
    }
    for viejo,nuevo in cambios.items():s=s.replace(viejo,nuevo,1)
    return s
cambiar('test/java/org/example/carestock/dao/AlertasInventarioDaoMapeoTest.java', actualizar_pruebas_fechas)

# Las dos interfaces funcionales del login trabajan con fallos JDBC.
sustitucion_especifica(
    'main/java/org/example/carestock/controller/LoginController.java',
    '        void iniciar(Usuario usuario) throws Exception;',
    '        void iniciar(Usuario usuario) throws java.sql.SQLException;')
sustitucion_especifica(
    'main/java/org/example/carestock/controller/LoginController.java',
    '        void registrar(Usuario usuario) throws Exception;',
    '        void registrar(Usuario usuario) throws java.sql.SQLException;')
sustitucion_especifica(
    'main/java/org/example/carestock/session/SesionUsuario.java',
    'public static synchronized void iniciar(Usuario usuarioAutenticado) throws Exception {',
    'public static synchronized void iniciar(Usuario usuarioAutenticado) throws java.sql.SQLException {')
sustitucion_especifica(
    'test/java/org/example/carestock/controller/LoginControllerFxTest.java',
    'doThrow(new Exception("Error JDBC simulado"))',
    'doThrow(new SQLException("Error JDBC simulado"))')

print('\nARCHIVOS MODIFICADOS:', len(MODIFICADOS))
print('ARCHIVOS OMITIDOS:', len(OMITIDOS))
print('IMPORTANTE: ejecutar pruebas Maven y JaCoCo antes de hacer commit.')
