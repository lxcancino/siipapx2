package sx.reportes


import grails.plugins.jasper.JasperExportFormat
import grails.plugins.jasper.JasperReportDef
import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import sx.core.Sucursal
import sx.reports.ReportService

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class VentasController {

    // static responseFormats = ['json', 'xml']


    ReportService reportService

    def ventasDiarias(PorFechaCommand command) {
        log.debug('Reporte de ventas diarias {}', params)
        def repParams = [:]
        repParams['ORIGEN'] = params.tipo
        repParams['SUCURSAL'] = params.SUCURSAL
        repParams['FECHA'] = command.fecha.format('yyyy/MM/dd')
        def pdf = this.reportService.run('ventas_diarias', repParams)
        def fileName = "VentasDiarias.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)
    }

    def cobranzaCod(PorFechaCommand command) {
        Map repParams = [:]
        repParams.FECHA = command.fecha.format('yyyy/MM/dd')
        repParams['SUCURSAL'] = params.SUCURSAL
        repParams['SALDOAFAVOR']=0.0
        def pdf = this.reportService.run('CobranzaCamioneta', repParams)
        def fileName = "CobranzaCOD.pdf"
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: fileName)

    }

    def cobranzaEfectivo(PorFechaCommand command) {
        Map repParams = [:]
        repParams.FECHA = command.fecha.format('yyyy/MM/dd')
        repParams['SUCURSAL'] = params.SUCURSAL
        def pdf = reportService.run('CobranzaEfectivo', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "CobranzaEfectivo.pdf")
    }

    def cobranzaContado(PorFechaCommand command){
        Map repParams = [:]
        repParams.FECHA = command.fecha.format('yyyy/MM/dd')
        repParams['SUCURSAL'] = params.SUCURSAL
        def pdf = reportService.run('FacturasCobrada', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "CobranzaContado.pdf")
    }

    def facturasCanceladas(){
        params.FECHA = params.fecha
        def pdf = reportService.run('FacturasCanceladas', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "FacturasCanceladas.pdf")
    }

    def aplicacionDeSaldos(PorFechaCommand command){
        // params.FECHA = params.fecha
        Map repParams = [:]
        repParams.FECHA = command.fecha.format('yyyy/MM/dd')
        repParams['SUCURSAL'] = params.SUCURSAL
        def pdf = reportService.run('AplicacionDeSaldos', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "AplicacionDeSaldos.pdf")
    }

    def disponiblesSucursal(){
        params.FECHA = params.fecha
        def pdf = reportService.run('DisponiblesSucursal', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "DisponiblesSucursal.pdf")
    }

    def facturasPendientesCod(){
        def pdf = reportService.run('fac_pen_camioneta', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "fac_pen_camioneta.pdf")
    }

    def facturasPendientesCodEmbarques(PorFechaCommand command){
        Map repParams = [:]
        repParams.FECHA = command.fecha.format('yyyy/MM/dd')
        repParams['SUCURSAL'] = params.SUCURSAL
        def pdf = reportService.run('fac_pen_camionetaNew', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "fac_pen_camionetaNew'.pdf")
    }

    def ventasDiariasCheques(PorFechaCommand command){
        Map repParams = [:]
        repParams.FECHA = command.fecha.format('yyyy/MM/dd')
        repParams['SUCURSAL'] = params.SUCURSAL
        def pdf = reportService.run('ventas_diariasCHE', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "VentasDiariasCheques.pdf")
    }


}


class PorFechaCommand {
    Date fecha

    String toString() {
        return fecha.format('dd/MM/yyyy')
    }
}




