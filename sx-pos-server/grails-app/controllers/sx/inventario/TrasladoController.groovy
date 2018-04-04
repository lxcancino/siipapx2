package sx.inventario

import com.luxsoft.cfdix.v33.TrasladoPdfGenerator
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.*
import grails.converters.*
import org.apache.commons.lang3.exception.ExceptionUtils
import sx.reports.ReportService

@Secured("ROLE_INVENTARIO_USER")
class TrasladoController extends RestfulController {

    static responseFormats = ['json']

    SolicitudDeTrasladoService solicitudDeTrasladoService

    TrasladoService trasladoService

    ReportService reportService

    TrasladoController() {
        super(Traslado)
    }

    @Override
    protected List listAllResources(Map params) {
        log.debug('Buscando trasaldos {}', params)
        params.max = 50;
        params.sort = 'lastUpdated'
        params.order = 'desc'

        def query = Traslado.where { }
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}
        }
        if(params.tipo) {
            query = query.where {tipo == params.tipo}
        }
        if (params.boolean('pendientes')) {
            params.sort = 'documento'
            params.order = 'asc'
            query = query.where {fechaInventario == null}
        }
        if(params.term){

            if(params.term.isInteger()) {
                String term = params.term
                log.debug('Buscando por documento {}', term)
                query = query.where{documento == term.toInteger()}
            } else {
                def search = "${params.term}%"
                log.debug('Buscando: {}', search)
                if (params.tipo == 'TPE') {
                    log.debug('Buscando {} por sucursal Atiende {}', params.tipo, search)
                } else {
                    log.debug('Buscando {} por sucursal Atiende {}', params.tipo, search)
                    query = query.where { solicitudDeTraslado.sucursalAtiende.nombre =~ search }
                }

            }
        }
        def list = query.list(params)
        return list
    }

    def salida(Traslado tps){
        if(tps == null) {
            notFound()
            return
        }
        assert tps.tipo == 'TPS', 'El registro de salida es para trasplados tipo TPS'
        def res = trasladoService.registrarSalida(tps)
        respond res

    }

    def entrada(Traslado tpe){
        if(tpe == null) {
            notFound()
            return
        }
        assert tpe.tipo == 'TPE', 'El registro de entrada es para trasplados tipo TPE'
        def res = trasladoService.registrarEntrada(tpe)
        respond res

    }

    def timbrar(Traslado tps) {
        if(tps == null) {
            notFound()
            return
        }
        assert tps.tipo == 'TPS', 'El timbrado es para TPS'
        def hoy = new Date()
        /*
        if( (hoy - tps.fechaInventario)  >= 2) {
            Map data = [:]
            data.message = "TPS ${tps.documento} fuera de tiempo para timbrado"
            respond data, status: 500
            return
        }
        */
        try {
            def res = trasladoService.timbrar(tps)
            respond res
        } catch (Exception ex) {
            def causa = ExceptionUtils.getRootCauseMessage(ex)
            log.error('Error timbrando TPS {}', ExceptionUtils.getMessage(ex))
            Map data = [:]
            data.message = ExceptionUtils.getRootCauseMessage(ex)
            respond data, status: 500
        }

    }

    def print() {
        params.TRALADO_ID = params.ID
        params.comentario = params.comentario
        Traslado traslado = Traslado.get(params.ID)
        params.SOL_ID = traslado.solicitudDeTraslado.id
        def reportName = traslado.tipo == 'TPS' ? 'SalidaDeTraslado.jrxml' : 'EntradaTraslado.jrxml'
        log.debug('Imprimiendo Traslado: {}-{} Usando reporte: {} params: {}', traslado.tipo, traslado.documento, reportName, params)
        def pdf =  reportService.run(reportName, params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Traslado.pdf')
    }

    def printCfdi() {

        Traslado tps = Traslado.get(params.ID)
        assert tps.tipo == 'TPS', 'Traslado no es un TPS'
        assert tps.cfdi, 'TPS no se ha timbrado'

        def realPath = servletContext.getRealPath("/reports") ?: 'reports'
        def data = TrasladoPdfGenerator.getReportData(tps)
        Map parametros = data['PARAMETROS']
        parametros.LOGO = realPath + '/PAPEL_CFDI_LOGO.jpg'
        parametros.IMPRESO_IMAGEN = realPath + '/Impreso.jpg'
        parametros.FACTURA_USD = realPath + '/facUSD.jpg'
        def pdf  = reportService.run('CFDITraslado.jrxml', data['PARAMETROS'], data['CONCEPTOS'])
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'CfdiTPS.pdf')
    }
}
