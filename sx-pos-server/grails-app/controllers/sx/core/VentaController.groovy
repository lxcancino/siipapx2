package sx.core

import com.luxsoft.utils.ImporteALetra
import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import groovy.transform.ToString
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.http.HttpStatus
import sx.cxc.CuentaPorCobrar
import sx.logistica.CondicionDeEnvio
import sx.logistica.Envio
import sx.reports.ReportService


@Secured("hasRole('ROLE_POS_USER')")
class VentaController extends RestfulController{

    static responseFormats = ['json']

    VentaService ventaService

    ReportService reportService

    VentaController(){
        super(Venta)
    }

    @Override
    protected List listAllResources(Map params) {
        params.sort = 'lastUpdated'
        params.order = 'desc'
        params.max = 500
        def query = Venta.where {}
        if(params.sucursal){
            query = query.where {sucursal.id ==  params.sucursal}
        }
        if (params.facturables) {
            query = query.where {facturar !=  null  && cuentaPorCobrar == null}
            if(params.facturables == 'CRE'){
                query = query.where {tipo == params.facturables}
            } else {
                query = query.where {tipo != 'CRE'}
            }
        }
        /*
        if (params.facturados) {
            log.debug('Buscando pedidos facturados {}', params)
            query = query.where {cuentaPorCobrar != null}
        }
        */
        def list = query.list(params)
        return list
    }


    @Transactional
    def mandarFacturar() {
        log.debug('Mandando facturar: ' + params)
        def res = ventaService.mandarFacturar(params.id)
        respond res
    }

    @Transactional
    def mandarFacturarConAutorizacion(AutorizacionDeVenta autorizacion) {
        // log.debug('Mandando facturar con autorizacion: {}', autorizacion)
        def res = ventaService.mandarFacturar(autorizacion.venta.id)
        autorizacion.save failOnError: true, flush: true
        respond res
    }

    @Transactional
    def asignarEnvio() {
        Venta venta = Venta.get(params.id)
        log.debug('Asignando envio para venta: {}', venta)
        Direccion direccion = new Direccion()
        bindData direccion, getObjectToBind()
        log.debug('Direccion asignada: {}', direccion)
        if(venta.envio) {
            venta.envio.direccion = direccion
        } else {
            log.debug('Venta sin envio, generando un envio nuevo para direccion {}', direccion)
            CondicionDeEnvio envio = new CondicionDeEnvio()
            envio.direccion = direccion
            envio.condiciones = 'ENVIO'
            envio.venta = venta
            venta.envio = envio
            venta = venta.save()
        }
        respond venta
    }

    @Transactional
    def cancelarEnvio() {
        Venta venta = Venta.get(params.id)
        log.debug('Cancelar envio para venta: {}', venta)
        if(venta.envio) {
            CondicionDeEnvio condicionDeEnvio= venta.envio
            venta.envio = null
            venta = venta.save failOnError: true, flush:true
            condicionDeEnvio.venta = null
            condicionDeEnvio.delete failOnError: true, flush: true
        }
        respond venta
    }


    @Override
    protected Object createResource() {
        Venta venta = new Venta()
        bindData venta, getObjectToBind()
        venta.partidas.each {
            if(it.corte)
                it.corte.ventaDet = it;
        }
        if(venta.envio) {
            venta.envio.venta = venta;
        }
        return venta
    }


    protected Venta saveResource(Venta resource) {
        log.debug('Salvando venta {}', resource.statusInfo())
        return ventaService.save(resource)
    }

    protected Venta updateResource(Venta resource) {
        log.debug('Actualizando venta: {}', resource)
        return ventaService.save(resource)
    }

    def pendientes(Sucursal sucursal) {
        if (sucursal == null) {
            notFound()
            return
        }
        params.max = params.registros ?:200
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def query = Venta.where{ sucursal == sucursal && cuentaPorCobrar == null && facturar == null}
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where{documento == params.term.toInteger()}
            } else {
                query = query.where { nombre =~ search }
            }
        }
        respond query.list(params)
    }


    def facturados(Sucursal sucursal) {
        log.debug('Buscando facturas {}', params )
        if (sucursal == null) {
            notFound()
            return
        }
        params.max = params.registros ?:100
        params.sort = params.sort ?:'documento'
        params.order = params.order ?:'desc'
        def query = Venta.where{ sucursal == sucursal && cuentaPorCobrar != null}
        if(params.boolean('canceladas')) {
            log.debug('Facturas canceladas {}', params)
            query = query.where {cuentaPorCobrar.cancelada != null}
        }
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where{cuentaPorCobrar.documento == params.term.toInteger()}
            } else {
                query = query.where { nombre =~ search }
            }
        }
        respond query.list(params)
    }

    def findManiobra() {
        def clave = params.clave
        def found = Producto.where{ clave == clave}.find()
        if(found == null ){
            notFound()
            return
        }
        respond found
    }

    @Transactional
    def facturar() {
        Venta pedido = Venta.get(params.id)
        assert !pedido.cuentaPorCobrar, 'Pedido ya facturado'
        pedido = ventaService.facturar(pedido);
        respond pedido
    }

    /**
     * Facturas de Contado, es decir tipo CON and COD
     * @param sucursal
     * @return
     */
    def cobradas(Sucursal sucursal) {
        if (sucursal == null) {
            notFound()
            return
        }
        // log.debug('Buscando facturas contado : {}', params)
        params.max = params.registros ?:100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def query = Venta.where{ sucursal == sucursal && cuentaPorCobrar != null}
        query = query.where { tipo != 'CRE'}
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where{cuentaPorCobrar.documento == params.term.toInteger()}
            } else {
                query = query.where { nombre =~ search }
            }
        }
        respond query.list(params)
    }

    @Transactional
    def timbrar() {
        Venta venta = Venta.get(params.id)
        try {
            def cfdi = ventaService.timbrar(venta)
            respond cfdi
            return
        }catch (Exception ex) {
            respond( [message: ExceptionUtils.getRootCauseMessage(ex)], status: HttpStatus.NOT_ACCEPTABLE )
        }
    }

    def print( ) {
        Venta venta = Venta.get(params.id)
        params.ID = venta.id
        params.IMP_CON_LETRA = ImporteALetra.aLetra(pedido.total)
        params.TELEFONOS = pedido.cliente.getTelefonos().join('/')
        if(pedido.envio) {
            params.DIR_ENTREGA = pedido.envio.direccion.toLabel()
        }
        if(pedido.socio) {
            params.SOCIO = pedido.socio.nombre
        }

        def pdf =  reportService.run('Pedido.jrxml', params)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Pedido.pdf')
    }

    /**
     * Cancela la venta, que implica eliminar la cuenta por cobrar pero deja el pedido  vivo
     *
     * @param venta
     * @return
     */
    def cancelar(Venta venta) {
        if(venta == null ){
            notFound()
            return
        }
        if (!venta.cuentaPorCobrar) {
            respond( [message: 'NO ESTA FACTURADA'], status: HttpStatus.NOT_ACCEPTABLE )
            return
        }
        String usuario = params.usuario
        String motivo = params.motivo
        // log.debug('Canccelando con: {}', params)

        try {
            def res = ventaService.cancelarFactura(venta, usuario, motivo)
            respond res
            return
        }catch (Exception ex) {
            respond( [message: ExceptionUtils.getRootCauseMessage(ex)], status: HttpStatus.NOT_ACCEPTABLE )
        }

        // respond venta

    }

    def cambioDeCliente(Venta venta){
        if(venta == null ){
            notFound()
            return
        }
        assert venta.cuentaPorCobrar == null , 'La venta esta facturada no se permite cambio de cliente'
        assert venta.tipo != 'CRE', 'Venta de credito no permite modificar cliente'
        assert venta.descuento <= venta.descuentoOriginal  ,
                "Venta con descuento especial, no permite cambio de cliente " +
                        " Dsc Original ${venta.descuentoOriginal} Desc: ${venta.descuento}"
        Cliente cliente = Cliente.get(params.cliente)
        String usuario = params.usuario
        venta.cliente = cliente
        venta.nombre = cliente.nombre
        venta.updateUser = usuario
        venta.save flush: true
        respond venta
    }

}

@ToString(includeNames=true,includePackage=false)
class VentasFiltro {

    Date fechaInicial
    Date fechaFinal
    Sucursal sucursal
    Cliente cliente
    int registros = 20


    static constraints = {
        fechaInicial nullable:true
        fechaFinal nullable: true
        sucursal nullable:true
        cliente nullable:true
        registros size:(1..500)
    }
}
