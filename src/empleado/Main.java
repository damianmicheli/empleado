package empleado;

import org.apache.log4j.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    
    // Instancio el objeto Logger para generar los logs.
    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        // Conecto a la base de datos
        Connection con = conectarBase();
        
        // Creo la tabla y genero un Statement
        Statement stmt = crearTabla(con);
        
        // Agrego un empleado con un id existente
        agregarRepetido(stmt);
        
        // Actualizo el nombre del empleado con el id indicado
        actualizarNombrePorId(3,"Roberto", stmt);
        
        // Elimino el empleado correspondiente al id indicado
        eliminarPorId(5, stmt);

        // Elimino el primer empleado que encuentre con la empresa indicada
        eliminarUnoPorEmpresa("Google", stmt);
        
        // Muestro la tabla en el estado actual
        mostrarTabla(stmt);
        
        // Cierro la conexión a la Base de Datos
        con.close();
        logger.info("Conexión a DB finalizada");
    }

    // Conecto con la base de datos y devuelvo un objeto tipo Connection
    public static Connection conectarBase(){

        // inicializo la conexión en null
        Connection con = null; 

        // Declaro mis datos de conexión.
        String url = "jdbc:h2:~/test";
        String user = "sa";
        String pass = "";

        // Busco el DriverManager para una base de datos H2 
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            // Si no lo encuentro genero error FATAL, muestro mensaje y el error que me escupe java (el objeto e)
            // y termino la ejecución
            logger.fatal("El driver no existe", e);
            System.exit(0);
        }
        
        // Genero la conexión informando los datos
        try {
            con = DriverManager.getConnection(url, user, pass);
            con.setAutoCommit(false); // Esto hace algo. Qué? No lo sé.
        } catch (SQLException e) {
            logger.fatal("No es posible conectarse", e);
            System.exit(0);
        }
        
        // Si tod0 salió bien, ya tengo la conexión creada. Devuelvo el objeto con la conexión.
        logger.info("Conexión a DB correcta");
        return con;
    }

    // Crea una tabla. Elimina la tabla si esta ya existe y la  vuelve a crear
    public static Statement crearTabla(Connection con){
        
        // Inicializo el Statement en null (si no hago esto al asignarle un objeto en el próximo try hay un error)
        Statement stmt = null;
        
        /* 
         * Declaro mis comandos SQL de creación de tabla en un string
         * Primero me fijo si ya existe la tabla. De ser así la elimina con DROP. 
         * Después crea la tabla con los campos indicados. El ID está puesto como PK autoincremental, es decir que cada
         * registro que agregue se va a generar un ID nuevo, por lo que no debo informarlo al insertar un registro.
         * Para insertar un registro sin informar el ID se deben nombrar explícitamente TODOS los campos que voy a 
         * insertar de ese registro, excepto el ID, claramente.
         * Después de la palabra VALUES van todos los registros nuevos entre paréntesis y separados por coma.
         */
        String sql = "DROP TABLE IF EXISTS EMPLEADO;\n" + // el \n en un string sirve para pasar a la siguiente linea
                "CREATE TABLE EMPLEADO(ID INT PRIMARY KEY AUTO_INCREMENT, NOMBRE VARCHAR(255), EDAD INT, EMPRESA VARCHAR(255), FECHA_INGRESO DATE);\n" +
                "INSERT INTO EMPLEADO (NOMBRE, EDAD, EMPRESA, FECHA_INGRESO) VALUES " +
                "('Damian', 42, 'Digital', '2022-11-20'), " +
                "('Mark', 46, 'Facebook', '2002-01-15'), " +
                "('Rafael', 33, 'Digital', '1999-01-15'), " +
                "('Miguel Angel', 36, 'Facebook', '1985-01-15'), " +
                "('Donatello', 56, 'Digital', '2010-01-15'), " +
                "('Larry', 50, 'Google', '1998-08-20');";
        
        // Creo el objeto Statement y lo uso para ejecutar el código SQL de arriba
        try {
            stmt = con.createStatement();   // CREO
            stmt.execute(sql);              // EJECUTO
        } catch (Exception e){
            logger.error("El código presenta un problema", e);
        }
        
        // al finalizar devuelvo el Statement porque lo voy a usar para todos los comandos SQL que quiera ejecutar
        return stmt;
    }

    /*
     *   Consigna: Debemos insertar tres filas con distintos datos, y una de ellas debe tener el ID repetido.
     *   Tenemos que verificar que nuestro log está mostrando este error —como los ID son primary keys,
     *   al intentar insertar el mismo id, nos va a dar un error, loguear el error—. 
     */
    
    // Trata de insertar un registro con el ID 2, que ya existe.
    public static void agregarRepetido(Statement stmt){
        
        // Escribo mi código SQL en un string
        String sql = "INSERT INTO EMPLEADO VALUES (2, 'Damian', 42, 'Digital', '2022-11-20');";
        
        // Ejecuto el código. Muestro el error como error en el Logger.
        try {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            logger.error("El código presenta un problema", e);
        }
    }

    /*
     *  Consigna: Luego, hay que actualizar a uno de los empleados con un nuevo valor en alguna de las columnas
     *  y loguear con un objeto debug toda la información del empleado.
     */

    // Actualiza el empleado con el ID indicado, con el Nombre que le pasamos
    public static void actualizarNombrePorId(int id, String nombre, Statement stmt){
        
        // Declaro el ResultSet (seria algo como "Conjunto de resultados"). Este no hace falta ponerlo en null
        ResultSet rs;

        // Creo dos objeto Empleado para guardar la información del registro (no le hice constructor, se crea vacío)
        // uno para guardar los datos en el estado original, otro ya cambiado
        Empleado empleado = new Empleado();
        Empleado empleadoCambiado = new Empleado();


        // Escribo un select para traer los datos del empleado que voy a modificar,
        // para guardar y mostrar los datos antes del cambio
        // DATO: esta query la armo dinámicamente con el ID que recibí por parámetro (MAGIA)
        String sql = "select * from EMPLEADO WHERE ID=" + id + ";";

        try {
            // Ejecuto la query SQL, el resultado (puede ser uno o mas registros) se guarda en el ResultSet rs.
            rs = stmt.executeQuery(sql);

            // Para acceder a los datos del primer registro de rs, tengo que usar next(). Lo mismo con los siguientes
            rs.next();

            // Ahora que tengo a mano el resultado, lo uso para armar mi objeto empleado

            // Si el dato esta en int, lo leo con el metodo getInt() del rs. Le puedo pasar nombre o numero de columna
            empleado.setId(rs.getInt("ID"));
            empleado.setEdad(rs.getInt("EDAD"));

            // Si el dato esta en String, lo leo con el metodo getString()
            empleado.setNombre(rs.getString("NOMBRE"));
            empleado.setEmpresa(rs.getString("EMPRESA"));

            // Si el dato esta en DATE, lo leo con el metodo getDate()... peeeero...
            // para guardar la fecha tengo que convertir del formato DATE de sql a LocalDate de JAVA: uso toLocalDate()
            empleado.setFechaIngreso(rs.getDate("FECHA_INGRESO").toLocalDate());

            // con logger muestro el empleado tal como esta actualmente (antes de cambiarlo) en una entrada tipo DEBUG
            // le paso empleado asi no más porque el toString() de la clase empleado se encarga del formato.
            logger.debug("Registro original: " + empleado);
        } catch (Exception e) {
            logger.error("Error en la consulta", e);
        }

        // Armo mi codigo SQL para modificar el empleado con el id que le pase, para ponerle el nombre que le pase
        String sqlUpdate = "UPDATE EMPLEADO SET NOMBRE='" + nombre + "' WHERE ID=" + id + ";";

        // Aca se usa executeUpdate(), porque estoy modificando
        // (esto devuelve un int con las filas afectadas. pero aca no lo uso)
        try {
            stmt.executeUpdate(sqlUpdate);
        } catch (Exception e) {
            logger.error("El código presenta un problema", e);
        }

        // Vuelvo a ejecutar la query que me trae datos el empleado del id indicado, y armo mi empleadoCambiado
        try {
            rs = stmt.executeQuery(sql);
            rs.next();
            empleadoCambiado.setId(rs.getInt("ID"));
            empleadoCambiado.setNombre(rs.getString("NOMBRE"));
            empleadoCambiado.setEmpresa(rs.getString("EMPRESA"));
            empleadoCambiado.setEdad(rs.getInt("EDAD"));
            empleadoCambiado.setFechaIngreso(rs.getDate("FECHA_INGRESO").toLocalDate());

            // Ahora muestro por logger el estado actual del empleado
            logger.debug("Registro modificado: " + empleadoCambiado);
        } catch (Exception e) {
            logger.error("Error en la consulta", e);
        }
    }

    /*
     *   Posteriormente, tenemos que eliminar un empleado según el ID y
     *   loguear como un objeto info toda la información del empleado eliminado.
     */

    public static void eliminarPorId(int id, Statement stmt){

        ResultSet rs;

        Empleado empleado = new Empleado();

        // Guardo los datos del empleado
        String sql = "select * from EMPLEADO WHERE ID=" + id + ";";

        try {
            rs = stmt.executeQuery(sql);
            rs.next();
            empleado.setId(rs.getInt("ID"));
            empleado.setNombre(rs.getString("NOMBRE"));
            empleado.setEmpresa(rs.getString("EMPRESA"));
            empleado.setEdad(rs.getInt("EDAD"));
            empleado.setFechaIngreso(rs.getDate("FECHA_INGRESO").toLocalDate());

        } catch (Exception e) {
            logger.error("Error en la consulta", e);
        }

        // Preparo el codigo para borrar el empleado
        String sqlDelete = "DELETE FROM EMPLEADO WHERE ID=" + id + ";";

        // Borro el empleado de la base de datos
        try {
            stmt.executeUpdate(sqlDelete);

            // muestro los datos del empleado que borre (los tenia guardados de antes)
            logger.info("Se eliminó el siguiente Registro: " + empleado);

        } catch (Exception e) {
            logger.error("El código presenta un problema", e);
        }
    }

    /*
     *   Consigna: Por último, eliminamos otro empleado según otra columna —por ejemplo,
     *   email— y loguear como un objeto info toda la información del empleado eliminado.
     */

    // Borra el primer empleado que encuentra que este en la empresa indicada
    public static void eliminarUnoPorEmpresa(String empresa, Statement stmt){

        ResultSet rs;

        Empleado empleado = new Empleado();

        String sql = "select * from EMPLEADO WHERE EMPRESA='" + empresa + "';";

        try {
            rs = stmt.executeQuery(sql);
            rs.next();
            empleado.setId(rs.getInt("ID"));
            empleado.setNombre(rs.getString("NOMBRE"));
            empleado.setEmpresa(rs.getString("EMPRESA"));
            empleado.setEdad(rs.getInt("EDAD"));
            empleado.setFechaIngreso(rs.getDate("FECHA_INGRESO").toLocalDate());

        } catch (Exception e) {
            logger.error("Error en la consulta", e);
        }

        String sqlDelete = "DELETE FROM EMPLEADO WHERE EMPRESA='" + empresa + "'";

        try {
            stmt.executeUpdate(sqlDelete);
            logger.info("Se eliminó el siguiente Registro: " + empleado);

        } catch (Exception e) {
            logger.error("El código presenta un problema", e);
        }

    }

    // Muestro todos los empleados de la tabla
    public static void mostrarTabla(Statement stmt){

        // Armo una lista de empleados vacía
        List<Empleado> empleados = new ArrayList<>();

        ResultSet rs;

        // Armo el sql para listar todos los empleados de la Base de datos
        String sql = "select * from EMPLEADO";

        // Ejecuto el codigo sql
        try {
            rs = stmt.executeQuery(sql);

            // como voy a tener varios registros, uso un while para que recorra todos (como si fuera un for each)
            while (rs.next()) {
                // Creo un empleado nuevo por cada registro que tenga en el ResultSet
                Empleado empleado = new Empleado();

                // Armo el empleado
                empleado.setId(rs.getInt("ID"));
                empleado.setNombre(rs.getString("NOMBRE"));
                empleado.setEmpresa(rs.getString("EMPRESA"));
                empleado.setEdad(rs.getInt("EDAD"));
                empleado.setFechaIngreso(rs.getDate("FECHA_INGRESO").toLocalDate());

                // Agrego el empleado a lista de empleados
                empleados.add(empleado);
            }

            // Armo un string para mostrar el resultado
            String salida = "Contenido de la tabla:";

            // Al string le voy concatenando todos los empleados, usando un for para recorrer la lista
            for (Empleado emp : empleados) {
                salida += "\n" + emp;
            }

            // Muestro la salida usando un INFO del logger
            logger.info(salida);
        } catch (Exception e) {
            logger.error("Error en la consulta", e);
        }
    }

}
