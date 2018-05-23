package sx.cxc

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured
import sx.core.Cliente
import sx.core.Sucursal
import sx.reports.ReportService

@Secured("hasRole('ROLE_POS_USER')")
class CuentaPorCobrarController extends RestfulController<CuentaPorCobrar>{

    static responseFormats = ['json']

    ReportService reportService

    CuentaPorCobrarService cuentaPorCobrarService

    AntiguedadService antiguedadService

    CuentaPorCobrarController() {
        super(CuentaPorCobrar)
    }

    @Override
    protected List listAllResources(Map params) {

        def query = CuentaPorCobrar.where {}
        params.sort = params.sort ?:'fecha'
        params.order = params.order ?:'asc'
        if(params.documento){
          int documento = params.int('documento')
          query = query.where { documento >= documento }
        }
        if(params.cliente){
            query = query.where { cliente.id == params.cliente}
        }
        return query.list(params)
    }

    def pendientes(Cliente cliente) {
        if (cliente == null) {
            notFound()
            return
        }
        // params.max = 100
        params.sort = params.sort ?:'fecha'
        params.order = params.order ?:'asc'
        def cartera = params.cartera ?: 'CRE'
        def rows = CuentaPorCobrar.findAll("from CuentaPorCobrar c  where c.cliente = ? and c.tipo = ? and c.total - c.pagos > 0 ", [cliente, cartera])
        respond rows
    }

    def antiguedad(){
        // Antiguedad de saldos
        log.info('Generando antiguedad de saldos {}', params);
        def rows = antiguedadService.antiguedad()
        respond rows
    }

    def saldar(CuentaPorCobrar cxc) {
        log.debug('Saldando cuenta por cobrar: {}', cxc.folio)
        cuentaPorCobrarService.saldar(cxc)
        cxc.refresh()
        respond cxc
    }

    def printAntiguedad() {
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def pdf = reportService.run('AntiguedadSaldosGral.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Antiguead.pdf')
    }

    def reporteDeCobranzaCOD(CobranzaCodCommand command) {
        def repParams = [:]
        repParams.FECHA = command.fecha.format('yyyy-MM-dd')
        repParams.SUCURSAL = command.sucursal.id
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def pdf = reportService.run('CarteraCOD_Emb.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'CarteraCOD.pdf')
    }

    def antiguedadPorCliente(AntiguedadPorCteCommand command) {
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def repParams = [:]
        repParams.FECHA_CORTE = command.fecha
        repParams.CLIENTE = command.cliente.id
        def pdf = reportService.run('AntiguedadSaldosConCortePorCliente.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Antiguead.pdf')
    }

    def clientesSuspendidosCre() {
        def repParams = [:]
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def pdf = reportService.run('ClientesSuspendidosCredito.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'ClientesSuspendidosCredito.pdf')
    }

    def facturasConNotaDevolucion(FacturasConNtaCommand command) {
        def repParams = [:]
        repParams.SUCURSAL = command.sucursal.id
        repParams.FECHA_INI = command.fechaIni
        repParams.FECHA_FIN = command.fechaFin
        repParams.ORIGEN = command.origen
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def pdf = reportService.run('FacsCancPorNotaDev.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'FacsCancPorNotaDev.pdf')

    }

    def reporteExceptionesDescuentos(FacturasConNtaCommand command) {
        def repParams = [:]
        repParams.SUCURSAL = command.sucursal.id
        repParams.FECHA_INI = command.fechaIni
        repParams.FECHA_FIN = command.fechaFin
        repParams.ORIGEN = command.origen
        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def pdf = reportService.run('ExcepcionesEnDescuento.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'FacsCancPorNotaDev.pdf')
    }




}

class CobranzaCodCommand {
    Sucursal sucursal
    Date fecha
}

class AntiguedadPorCteCommand {
    Date fecha
    Cliente cliente
}

class FacturasConNtaCommand {
    Date fechaIni
    Date fechaFin
    Sucursal sucursal
    String origen
}
