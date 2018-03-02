package sx.admin.server

class UrlMappings {

    static mappings = {
        delete "/$controller/$id(.$format)?"(action:"delete")
        get "/$controller(.$format)?"(action:"index")
        get "/$controller/$id(.$format)?"(action:"show")
        post "/$controller(.$format)?"(action:"save")
        put "/$controller/$id(.$format)?"(action:"update")
        patch "/$controller/$id(.$format)?"(action:"patch")

        // Catalogos principales
        "/api/sucursales"(resources: "sucursal")
        "/api/sucursales/otrosAlmacenes"(controller: "sucursal", action: 'otrosAlmacenes', method: 'GET')
        "/api/lineas"(resources: "linea")
        "/api/marcas"(resources: "marca")
        "/api/clases"(resources: "clase")
        "/api/productos"(resources: "producto")
        "/api/proveedores"(resources: "proveedor"){
            "/productos"(resources:'proveedorProducto')
        }
        "/api/clientes"(resources: "cliente"){
            "/credito"(resources: 'clienteCredito')
        }
        "/api/clientes/actualizarCfdiMail/$id"(controller: "cliente", action: 'actualizarCfdiMail', method: 'PUT')
        "/api/clientes/$id/facturas"(controller: 'cliente', action: 'facturas', method: 'GET')
        "/api/clientes/$id/cxc"(controller: 'cliente', action: 'cxc', method: 'GET')
        "/api/clientes/$id/cobros"(controller: 'cliente', action: 'cobros', method: 'GET')


        // SAT
        "/api/sat/bancos"(resources: "SatBanco")
        "/api/sat/cuentas"(resources:"SatCuenta")

        // Tesoreria
        "/api/tesoreria/bancos"(resources: "banco")
        "/api/tesoreria/cuentas"(resources: "cuentaDeBanco")

        //Comprobantes fiscales de proveedores CFDI's
        "/api/cfdis"(resources: "cfdi")
        "/api/cfdis/mostrarXml/$id?"(controller:"cfdi", action:"mostrarXml")
        "/api/cfdis/print/$id"(controller: "cfdi", action: 'print', method: 'GET')
        "/api/cfdis/enviarEmail/$id?"(controller:"cfdi", action:"enviarEmail")

        // Compras
        "/api/compras"(resources: "compra")
        "/api/compras/print/$ID"(controller: 'compra', action: 'print', method: 'GET')

        // Ventas
        "/api/ventas"(resources:"venta")

        "/api/tesoreria/solicitudes"(resources:"solicitudDeDeposito")
        "/api/tesoreria/solicitudes/pendientes"( controller: 'solicitudDeDeposito', action: 'pendientes')
        "/api/tesoreria/solicitudes/autorizadas"( controller: 'solicitudDeDeposito', action: 'autorizadas')
        "/api/tesoreria/solicitudes/transito"( controller: 'solicitudDeDeposito', action: 'transito')
        "/api/tesoreria/solicitudes/canceladas"( controller: 'solicitudDeDeposito', action: 'canceladas')
        "/api/tesoreria/solicitudes/autorizar/$id"( controller: 'solicitudDeDeposito', action: 'autorizar')
        "/api/tesoreria/solicitudes/posponer/$id"( controller: 'solicitudDeDeposito', action: 'posponer')
        "/api/tesoreria/solicitudes/rechazar/$id"( controller: 'solicitudDeDeposito', action: 'rechazar')
        "/api/tesoreria/solicitudes/cancelar/$id"( controller: 'solicitudDeDeposito', action: 'cancelar')
        "/api/tesoreria/solicitudes/buscarDuplicada/$id"( controller: 'solicitudDeDeposito', action: 'buscarDuplicada')

        // CXC
        "/api/cxc/cobro"(resources: "cobro")
        "/api/cxc/cobro/cobrosMonetarios"(controller: "cobro", action: 'cobrosMonetarios', method: 'GET')
        "/api/cxc/cobro/reporteDeComisionesTarjeta"(controller: 'cobro', action: 'reporteDeComisionesTarjeta', method: 'GET')
        "/api/cxc/cobro/aplicar/$id"(controller: "cobro", action: 'aplicar', method: 'PUT')


        "/api/cxc/notas"(resources: "notaDeCredito")
        "/api/cxc/notas/buscarRmd"(controller: "notaDeCredito", action: 'buscarRmd', method: 'GET')
        "/api/cxc/notas/buscarFacturasPendientes"(controller: "notaDeCredito", action: 'buscarFacturasPendientes', method: 'GET')
        "/api/cxc/notas/generarConRmd/$id"(controller: 'notaDeCredito', action: 'generarConRmd', method: 'POST')
        "/api/cxc/notas/timbrar/$id"(controller: 'notaDeCredito', action: 'timbrar', method: 'POST')
        "/api/cxc/notas/aplicar/$id"(controller: 'notaDeCredito', action: 'aplicar', method: 'POST')
        "/api/cxc/notas/print/$id"(controller: 'notaDeCredito', action: 'print', method: 'GET')

        "/api/cxc/notasDeCargo"(resources: "notaDeCargo")
        "/api/cxc/notasDeCargo/timbrar/$id"(controller: 'notaDeCargo', action: 'timbrar', method: 'POST')
        "/api/cxc/notasDeCargo/print/$id"(controller: 'notaDeCargo', action: 'print', method: 'GET')


        "/api/cuentasPorCobrar"(resources: 'cuentaPorCobrar')
        "/api/cuentasPorCobrar/pendientes/$id"(controller: 'cuentaPorCobrar', action: 'pendientes', method: 'GET')



        //Existencias
        "/api/existencias"(resources: "existencia")
        "/api/existencias/$producto/$year/$month"(controller: 'existencia', action: 'buscarExistencias')

        // Security
        "/api/security/users"(resources: "user")
        "/api/security/roles"(resources: "role")
        "/api/security/users/findByNip"( controller:'user', action: 'findByNip', method: 'GET')

        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
