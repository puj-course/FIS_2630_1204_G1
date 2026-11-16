#!/usr/bin/env python3
"""Limpieza conservadora de hallazgos Sonar CareStock (16 nov 2026).

Ejecutar desde la raiz del repositorio; comprueba patrones antes de escribir.
No modifica reglas de inventario ni SQL. Puede ejecutarse varias veces.
"""
from __future__ import annotations

from pathlib import Path
import re
import sys

ROOT = Path.cwd()
JAVA = ROOT / 'src/CareStock/src'
if not (ROOT / 'src/CareStock/pom.xml').exists():
    sys.exit('ERROR: ejecutar en la raiz FIS_2630_1204_G1 (no en src/CareStock)')

changes = []


def edit(rel, function):
    p = JAVA / rel
    if not p.is_file():
        print('OMITIDO (no existe):', rel)
        return
    original = p.read_bytes().decode('utf-8')
    line_ending = '\r\n' if '\r\n' in original else '\n'
    before = original.replace('\r\n', '\n')
    after = function(before)
    if before != after:
        p.write_bytes(after.replace('\n', line_ending).encode('utf-8'))
        changes.append(str(p.relative_to(ROOT)))
        print('CORREGIDO:', rel)


# Imports señalados explícitamente en el reporte.
unused_imports = {
    'controller/NavegacionRefactorizadaTest.java': 'org.junit.jupiter.api.Assertions',
    'dao/HistorialMovimientosDaoProfundoTest.java': 'org.mockito.ArgumentMatchers',
    'dao/UsuarioDAOImplCoverage80Test.java': 'org.mockito.ArgumentMatchers',
    'service/FarmacovigilanciaCoverage87Test.java': 'org.mockito.ArgumentMatchers',
    'service/ServicioAjustesJdbcCoverage87Test.java': 'org.mockito.ArgumentMatchers',
    'service/ServicioMovimientosJdbcCoverage87Test.java': 'org.mockito.ArgumentMatchers',
    'sms/ServicioDirectorioSmsCoberturaExtraTest.java': 'org.mockito.ArgumentMatchers',
}
for filename, import_name in unused_imports.items():
    line = f'import {import_name};'
    def quitar_import(s, line=line, import_name=import_name):
        sin_import = s.replace(line + '\n', '')
        simbolo = import_name.split('.')[-1]
        # No eliminar si la versión actual sí emplea el nombre importado.
        return sin_import if not re.search(r'\b' + simbolo + r'\s*\.', sin_import) else s
    edit(f'test/java/org/example/carestock/{filename}', quitar_import)

# Declaraciones 'throws Exception' que Sonar identifica como no necesarias.
throws_targets = {
    'config/ConexionBDCoverage87Test.java': (
        'restaurar', 'establecer', 'parametroObligatorioAusenteLanzaError',
        'parametroObligatorioEnBlancoLanzaError',
        'enteroNoNumericoProduceSQLException',
        'enteroFueraDeRangoNumericoProduceSQLException'),
    'controller/CatalogoInteraccionCoverageTest.java': ('consultaFallidaNoEntregaIndicadoresFalsos',),
    'controller/ControladoresProductosCoberturaTest.java': (
        'presentaProductosDeAseo', 'presentaProductosDeMaternidad', 'presentaMedicamentos'),
    'dao/MedicamentoCatalogoJdbcTest.java': ('validaCamposObligatorios',),
    'facade/InventarioFachadaCoberturaTest.java': (
        'rechazaMedicamentoSinCategoria', 'rechazaMedicamentoNuloYSinPrincipioActivo'),
}

def remove_throws(s, names):
    for name in names:
        pat = re.compile(r'(\b' + re.escape(name) + r'\s*\([^;{}]*?\))\s*throws\s+Exception\b[ \t]*')
        s, n = pat.subn(lambda m: m.group(1) + ' ', s, count=1)
        if n: print('  throws eliminado:', name)
    return s

for filename, names in throws_targets.items():
    edit(f'test/java/org/example/carestock/{filename}',
         lambda s, names=names: remove_throws(s, names))

# Variable local nunca utilizada en Mockito.
def remove_unused_sql(s):
    pat = re.compile(r'(?m)^\s*String sql = inv\.getArgument\(0\);\s*\n')
    if len(re.findall(r'\bsql\b', s)) == 1:
        return pat.sub('\n', s, count=1)
    return s
edit('test/java/org/example/carestock/service/ServicioCrudLotesJdbcTest.java', remove_unused_sql)

# Mockito: crear mock por separado, sin ocultar su creación dentro del stubbing.
def fix_movimientos_mock(s):
    pat = re.compile(r'(?m)^(\s*)(s\.when\(SesionUsuario::usuarioParaFachada\)\s*\.thenReturn\()mock\(Usuario\.class\)(\);)')
    return pat.sub(lambda m: m.group(1) + 'Usuario usuarioSimulado = mock(Usuario.class);\n'
                           + m.group(1) + m.group(2) + 'usuarioSimulado' + m.group(3), s)
for filename in ('controller/ControladorMovimientosCargaCoverage87Test.java',
                 'controller/ontroladorMovimientosVistaPreviaTest.java'):
    edit(f'test/java/org/example/carestock/{filename}', fix_movimientos_mock)

def fix_catalogo_mock(s):
    pat = re.compile(r'(?m)^(\s*)(when\(loader\.getController\(\)\)\s*\.thenReturn\()mock\(ControladorCatalogo\.class\)(\);)')
    return pat.sub(lambda m: m.group(1) + 'ControladorCatalogo controladorSimulado = mock(ControladorCatalogo.class);\n'
                          + m.group(1) + m.group(2) + 'controladorSimulado' + m.group(3), s)
edit('test/java/org/example/carestock/controller/CatalogoCoverageFocalizadoTest.java', fix_catalogo_mock)

# FechaNegocioTest: esperado primero, obtenido segundo (solo si están al revés).
def fix_assertequals(s):
    return s.replace('assertEquals(FechaNegocio.ZONA, ZoneId.of("America/Bogota"))',
                     'assertEquals(ZoneId.of("America/Bogota"), FechaNegocio.ZONA)')
edit('test/java/org/example/carestock/utils/FechaNegocioTest.java', fix_assertequals)

# MedicamentoDAO: una constante para el fragmento SELECT repetido.
def fix_select_constant(s):
    occurrences = s.count('"SELECT "')
    if occurrences < 3 or 'private static final String SELECT_SQL' in s:
        return s
    found = re.search(r'(\b(?:public\s+)?class\s+MedicamentoDAOImpl\b[^\{]*\{)', s)
    if not found:
        return s
    s = s[:found.end()] + '\n    private static final String SELECT_SQL = "SELECT ";\n' + s[found.end():]
    return s.replace('"SELECT "', 'SELECT_SQL').replace('SELECT_SQL = SELECT_SQL;', 'SELECT_SQL = "SELECT ";')
edit('main/java/org/example/carestock/dao/MedicamentoDAOImpl.java', fix_select_constant)

# Elimina un método privado sin uso solo si NO está referenciado en la clase.
def remove_unused_method(s):
    pat = re.compile(r'(?m)^\s*private\s+void\s+cargarCategorias\s*\(\s*\)\s*\{')
    m = pat.search(s)
    if not m or len(re.findall(r'\bcargarCategorias\s*\(', s)) != 1:
        return s
    depth = 0
    for j in range(s.index('{', m.start()), len(s)):
        if s[j] == '{':depth += 1
        elif s[j] == '}':
            depth -= 1
            if depth == 0:
                return s[:m.start()] + s[j+1:]
    return s
edit('main/java/org/example/carestock/controller/ControladorInventario.java', remove_unused_method)

# JavaFX: el Timeline solo se necesita localmente en configurarRefresco.
def fix_timeline(s):
    if len(re.findall(r'\brefresco\b', s)) != 5:
        return s  # en otra version puede ser compartido: no tocar
    if not re.search(r'(?m)^\s*private Timeline refresco;', s):
        return s
    s = re.sub(r'(?m)^\s*private Timeline refresco;\n', '', s, count=1)
    return s.replace('        refresco = new Timeline(new KeyFrame(INTERVALO_REFRESCO, e -> cargar()));',
                     '        Timeline refresco = new Timeline(new KeyFrame(INTERVALO_REFRESCO, e -> cargar()));', 1)
edit('main/java/org/example/carestock/controller/ControladorAlertas.java', fix_timeline)

# ChangeListener usa Boolean, pero Sonar pide una expresión boolean primitiva.
def fix_boolean_login(s):
    if s.count('if (!ahora)') == 3:
        return s.replace('if (!ahora)', 'if (Boolean.FALSE.equals(ahora))')
    return s
edit('main/java/org/example/carestock/controller/LoginController.java', fix_boolean_login)

# Busca cierre paréntesis respetando caracteres en strings de Java.
def end_of_paren(s, open_at):
    if s[open_at] != '(':
        return -1
    d = 0
    quote = None
    escape = False
    for i in range(open_at, len(s)):
        c = s[i]
        if quote:
            if escape: escape = False
            elif c == '\\': escape = True
            elif c == quote: quote = None
            continue
        if c in ('"', "'"):
            quote = c
        elif c == '(':
            d += 1
        elif c == ')':
            d -= 1
            if d == 0:return i
    return -1

# Test de assertAll: preparar entidades antes de crear las lambdas.
def fix_assertall_factory(s, method, factory, tiponuevo, cantidad):
    method_pos = s.find('void ' + method + '(')
    if method_pos < 0: return s
    begin = s.find('assertAll(', method_pos)
    if begin < 0: return s
    close = end_of_paren(s, s.index('(', begin))
    if close < 0: return s
    body = s[begin:close+1]
    pat = re.compile(r'\b' + re.escape(factory) + r'\(\)')
    if len(pat.findall(body)) != cantidad:
        return s
    # Solo sustituir entidades construidas dentro de los assertThrows.
    declarations=[]
    for i in range(cantidad):
        variable=f'sonarObjetoInvalido{i+1}'
        declarations.append(tiponuevo+' '+variable+' = '+factory+'();')
        body=pat.sub(variable, body, count=1)
    line_start=s.rfind('\n',0,begin)+1
    indent=s[line_start:begin]
    if indent.strip():
        return s
    return s[:line_start] + ''.join(indent+d+'\n' for d in declarations) + '\n' + indent + body+s[close+1:]

edit('test/java/org/example/carestock/dao/CatalogoDaoJdbcTest.java',
     lambda s: fix_assertall_factory(s,'rechazaRegistrosYContextosInvalidos','aseo','Aseo',2))
edit('test/java/org/example/carestock/dao/MedicamentoCatalogoJdbcTest.java',
     lambda s: fix_assertall_factory(s,'rechazaIdentificadoresNoValidos','medicamento','Medicamento',3))

# Lambdas: mueve fuera de assertThrows precondiciones seguras, sin mover la acción verificada.
lambda_files = (
    'CareStockAppGestion984Test.java', 'CareStockAppNavegacionAlertasTest.java',
    'DataTransferObject/DatosTransferenciaTest.java',
    'controller/ControladorAlertasCoberturaMainTest.java',
    'controller/MovimientosContextuales984Test.java',
    'dao/ConsultaInventarioDaoCoberturaTest.java',
    'dao/HistorialMovimientosDaoProfundoTest.java',
    'dao/UsuarioDAOImplCoverage80Test.java',
    'model/AjusteExistenciasTest.java', 'model/ProductosBasicosTest.java',
    'service/AuthenticationServiceTest.java',
    'service/ExportacionReposicionCasosLimiteTest.java',
    'service/ExportacionReposicionTest.java',
    'service/PoliticaAccionesAlertasTest.java',
    'service/ResolucionNavegacionAlertasTest.java',
    'service/ServicioCrudLotesJdbcTest.java',
    'service/ServicioCrudLotesValidacionesTest.java',
    'session/SesionUsuarioCoberturaAdicionalTest.java',
    'sms/FabricaServicioSmsCoverage87Test.java',
    'sms/FormateadorInicioSesionSmsTest.java',
    'sms/RegistroInicioSesionSmsTest.java',
    'sms/ReglasSmsCoverage87Test.java',
    'sms/RepositorioPreferenciasSmsJdbcCoberturaTest.java',
    'sms/ServicioAlertasSmsCoberturaTest.java',
    'sms/ServicioDirectorioSmsCoberturaExtraTest.java',
    'sms/ServicioSmsSimpleAndroidCoberturaExtraTest.java',
    'sms/SmsCurlCoberturaExtraTest.java',
)

helper_call = re.compile(r'\b(?:stock|vencimiento|destino|aseo|medicamento|permitidos|autorizados|simular|servicio|actor|alertaStock|usuario|solicitud|sesion|crear)\s*\(')
static_call = re.compile(r'\b(?:FechaNegocio\.hoy|Instant\.now|Set\.of|Map\.of|List\.of|Arrays\.asList)\s*\(')


def choose_precondition(expr):
    """Devuelve (texto literal original, expresión de inicialización) o None."""
    m = re.search(r'\bnew\s+([\w.]+)\s*\(\s*\)\s*(?=\.)', expr)
    if m:
        return (m.group(0).rstrip(), m.group(0).rstrip())
    m = re.search(r'\bnew\s+SesionUsuario\.DatosUsuario\s*\(', expr)
    if m and m.start() > 0:
        close = end_of_paren(expr, expr.index('(', m.start()))
        if close > 0:
            return (expr[m.start():close+1], expr[m.start():close+1])
    m = re.search(r'\bdatos\.(?:lotes|categorias|productos|categoriasMedicamento)\(\)\s*(?=\.)', expr)
    if m:
        return (m.group(0).rstrip(), m.group(0).rstrip())
    m = re.search(r'"[^"]+"\.repeat\(\d+\)', expr)
    if m:
        return (m.group(0), m.group(0))
    matches = list(helper_call.finditer(expr)) + list(static_call.finditer(expr))
    # Evitar extraer el método que es, él mismo, el objeto de la prueba.
    matches = [m for m in matches if m.start() > 0 and expr[:m.start()].strip() and expr[m.start()-1] != '.']
    matches.sort(key=lambda m: m.start())
    for m in matches:
        op = expr.index('(', m.start())
        close = end_of_paren(expr, op)
        if close < 0:continue
        val = expr[m.start():close+1]
        if val in ('Set.of()', 'List.of()', 'Map.of()'):
            continue  # preservar inferencia generica de la llamada original
        if val.startswith('FechaNegocio.hoy'):
            chain = re.match(r'\s*\.(?:plusDays|minusDays|plusMonths)\s*\(', expr[close+1:])
            if chain:
                extra_start = close+1+chain.end()-1
                extra_close = end_of_paren(expr, extra_start)
                if extra_close > 0:
                    val = expr[m.start():extra_close+1]
        return (val, val)
    # Si el propio constructor tiene un método adicional, preparar el objeto por separado.
    return None


def fix_assertthrows(s):
    count=0
    next_id=max([0]+[int(n) for n in re.findall(r'\bsonarPrecondicion(\d+)\b', s)])+1
    cursor=0
    while True:
        m = re.search(r'\bassertThrows\s*\(', s[cursor:])
        if not m:break
        start=cursor+m.start()
        op=s.index('(',start)
        end=end_of_paren(s,op)
        if end<0:break
        block=s[op+1:end]
        arrow=re.search(r'\(\s*\)\s*->\s*',block)
        if not arrow:
            cursor=end+1;continue
        expr=block[arrow.end():].strip()
        if expr.startswith('{'):
            cursor=end+1;continue
        choice=choose_precondition(expr)
        if not choice:
            cursor=end+1;continue
        original, init_expr=choice
        if original not in expr:
            cursor=end+1;continue
        # No modificar assertions dentro de lambdas de assertAll: requieren reestructuración manual.
        prefix=s[s.rfind('\n',0,start)+1:start]
        if '->' in prefix:
            cursor=end+1;continue
        # No alterar si el método auxiliar es la propia invocación que debe lanzar la excepción.
        if expr.strip() == original:
            cursor=end+1;continue
        line_start=s.rfind('\n',0,start)+1
        indent=re.match(r'\s*',s[line_start:start]).group()
        if '\t' in indent: indent='    '
        ident=f'sonarPrecondicion{next_id}'
        next_id += 1
        new_expr=expr.replace(original,ident,1)
        new_block=block[:arrow.end()]+block[arrow.end():].replace(expr,new_expr,1)
        replacement='assertThrows('+new_block+')'
        before=s[:line_start]
        after=s[line_start:start]+replacement+s[end+1:]
        s=before+indent+'final var '+ident+' = '+init_expr+';\n'+after
        count+=1
        cursor=line_start+len(indent)+len('final var '+ident+' = '+init_expr+';\n')+(start-line_start)+len(replacement)
    if count:print('  lambdas simplificadas:',count)
    return s

def fix_all_assertthrows(s):
    # Una misma aserción puede tener dos precondiciones (p. ej. fecha y constructor).
    for _ in range(3):
        fixed = fix_assertthrows(s)
        if fixed == s:
            break
        s = fixed
    return s

for filename in lambda_files:
    edit(f'test/java/org/example/carestock/{filename}', fix_all_assertthrows)

print(f'\nArchivos modificados: {len(changes)}')
if not changes:
    print('Sin modificaciones; revisa coincidencia con la versión actual del reporte.')
else:
    print('\nValidar con: (cd src/CareStock && mvn -B clean verify)')
    print('Luego ejecutar el analizador SonarQube/SonarCloud y revisar el Quality Gate.')
