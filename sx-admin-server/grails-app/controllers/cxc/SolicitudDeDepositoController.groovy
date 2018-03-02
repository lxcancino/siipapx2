package sx.cxc

import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import groovy.transform.ToString
import grails.plugin.springsecurity.annotation.Secured
import sx.core.AppConfig
import sx.core.Folio
import sx.core.Sucursal
import sx.tesoreria.Banco
import sx.tesoreria.CuentaDeBanco
import sx.tesoreria.SolicitudDeDepositoService


@Secured("hasRole('ROLE_POS_USER')")
class SolicitudDeDepositoController extends RestfulController{

    static responseFormats = ['json']

    SolicitudDeDepositoService solicitudDeDepositoService;

    SolicitudDeDepositoController(){
        super(SolicitudDeDeposito)
    }

    @Override
    protected List listAllResources(Map params) {
        // log.debug('List: {}', params)
        params.max = params.registros ?:1000
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        def query = SolicitudDeDeposito.where {}
        if (params.cartera) {
            query = query.where { tipo == params.cartera}
        }
        if (params.pendientes) {
            if (params.boolean('pendientes')){
                log.debug('Buscando solicitudes pendientes')
                query = query.where {cobro == null}
            } else {
                log.debug('Buscando solicitudes atendidas')
                query = query.where {cobro != null}
            }
        }
        if(params.term) {
            def search = '%' + params.term + '%'
            if(params.term.isInteger()) {
                query = query.where { folio == params.term.toInteger() }
            } else {
                query = query.where { cliente.nombre =~ search || banco.nombre =~ search  }
            }
        }
        return query.list(params)
    }

    def pendientes() {
        // log.debug('Buscando solicitudes pendientes2 {}', params)
        params.max = params.registros ?:100
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'asc'

        def query = SolicitudDeDeposito.where {
            cobro == null && comentario == null && cancelacion == null
        }
        def list = query.list(params)
        // log.debug('Solicitudes pendientes: {}', list.size())
        respond list
    }

    def autorizadas(SolicitudFilter filter) {
        // log.debug('Buscando solicitudes autorizadas {}', params)
        // log.debug('Filter: {}', filter)
        params.max = 50
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'

        def query = SolicitudDeDeposito.where {
            cobro != null
        }
        if(params.cartera) {
            query = query.where { tipo == params.cartera}
        }

        if( params.folio && params.folio.isInteger()){
            // log.debug('Buscando por Folio: ', params.folio.toInteger())
            query = query.where { folio == params.folio.toInteger() }
        }

        if( params.total && params.total.isBigDecimal()){
            // log.debug('Buscando por total: ', params.total.toBigDecimal())
            query = query.where { total == params.total.toBigDecimal() }
        }

        if(filter.fechaDeposito) {
            // log.debug('Buscando por fecha: {}', filter.fechaDeposito)
            query = query.where { fechaDeposito == filter.fechaDeposito }
        }

        if (params.cliente) {
            // log.debug('Filtrando por cliente')
            String search = '%' + params.cliente + '%'
            query = query.where { cliente.nombre =~ search  }
        }

        if (params.sucursal) {
            // log.debug('Filtrando por sucursal: {}', params.sucursal)
            String search = '%' + params.sucursal + '%'
            query = query.where { sucursal.nombre =~ search  }
        }
        if (params.banco) {
            String search = '%' + params.banco+ '%'
            query = query.where { cuenta.descripcion =~ search  }
        }


        def list = query.list(params)
        respond list
    }

    def transito(SolicitudFilter filter) {
        // log.debug('Buscando solicitudes transito {}', params)
        // log.debug('Filter: {}', filter)
        params.max = 50
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'

        def query = SolicitudDeDeposito.where {
            cobro == null && comentario != null
        }

        if( params.folio && params.folio.isInteger()){
            log.debug('Buscando por Folio: ', params.folio.toInteger())
            query = query.where { folio == params.folio.toInteger() }
        }

        if( params.total && params.total.isBigDecimal()){
            log.debug('Buscando por total: ', params.total.toBigDecimal())
            query = query.where { total == params.total.toBigDecimal() }
        }

        if(filter.fechaDeposito) {
            // log.debug('Buscando por fecha: {}', filter.fechaDeposito)
            query = query.where { fechaDeposito == filter.fechaDeposito }
        }

        if (params.cliente) {
            // log.debug('Filtrando por cliente')
            String search = '%' + params.cliente + '%'
            query = query.where { cliente.nombre =~ search  }
        }

        if (params.sucursal) {
            // log.debug('Filtrando por sucursal: {}', params.sucursal)
            String search = '%' + params.sucursal + '%'
            query = query.where { sucursal.nombre =~ search  }
        }

        def list = query.list(params)
        respond list
    }

    def canceladas(SolicitudFilter filter) {
        // log.debug('Buscando solicitudes transito {}', params)
        // log.debug('Filter: {}', filter)
        params.max = 50
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'

        def query = SolicitudDeDeposito.where {
            cancelacion!= null
        }

        if( params.folio && params.folio.isInteger()){
            // log.debug('Buscando por Folio: ', params.folio.toInteger())
            query = query.where { folio == params.folio.toInteger() }
        }

        if( params.total && params.total.isBigDecimal()){
            // log.debug('Buscando por total: ', params.total.toBigDecimal())
            query = query.where { total == params.total.toBigDecimal() }
        }

        if(filter.fechaDeposito) {
            // log.debug('Buscando por fecha: {}', filter.fechaDeposito)
            query = query.where { fechaDeposito == filter.fechaDeposito }
        }

        if (params.cliente) {
            // log.debug('Filtrando por cliente')
            String search = '%' + params.cliente + '%'
            query = query.where { cliente.nombre =~ search  }
        }

        if (params.sucursal) {
            // log.debug('Filtrando por sucursal: {}', params.sucursal)
            String search = '%' + params.sucursal + '%'
            query = query.where { sucursal.nombre =~ search  }
        }

        def list = query.list(params)
        respond list
    }

    @Transactional
    def autorizar(SolicitudDeDeposito sol) {
        // log.debug('Autorizando solicitud de deposito {}', params.id)
        def res = solicitudDeDepositoService.autorizar(sol)
        respond res;
    }

    @Transactional
    def posponer(SolicitudDeDeposito sol) {
        // log.debug('Posponiendo sol: ', sol);
        sol.sw2 = new Date().toString();
        sol.save flush:true
        respond sol
    }

    @Transactional
    def rechazar(SolicitudDeDeposito sol) {
        sol.comentario = params.comentario
        updateResource sol
        respond sol
    }

    @Transactional
    def cancelar(SolicitudDeDeposito sol) {
        sol.cancelacionComentario = params.comentario
        sol.cancelacion = new Date()
        updateResource sol
        respond sol
    }


    @Override
    protected Object createResource() {
        SolicitudDeDeposito sol = new SolicitudDeDeposito()
        bindData sol, getObjectToBind()
        sol.sucursal = Sucursal.where { clave == 1}.find()
        sol.fecha = new Date()
        return sol
    }

    protected SolicitudDeDeposito saveResource(SolicitudDeDeposito resource) {
        resource.total = resource.cheque + resource.efectivo + resource.transferencia
        // log.debug('Salvando solicitud: {}', resource)
        if(resource.id == null) {
            def serie = resource.sucursal.nombre
            resource.folio = Folio.nextFolio('SOLICITUDES_DEPOSITO',serie)
        }
        return super.saveResource(resource)
    }

    def buscarDuplicada(SolicitudDeDeposito instance){
        //log.debug('Buscando posible solicitud duplicada {}', instance.folio)

        def duplicada = SolicitudDeDeposito.where{
            id!= instance.id && total == instance.total && banco == instance.banco && cuenta == instance.cuenta && fechaDeposito == instance.fechaDeposito
        }.find()
        // log.debug('Duplicada: ', duplicada)

        respond duplicada?: ['OK']
    }


}

@ToString(includeNames=true,includePackage=false)
class SolicitudFilter {
    Date fechaDeposito

    static constraints = {
        fechaDeposito nullable: true
    }

}

