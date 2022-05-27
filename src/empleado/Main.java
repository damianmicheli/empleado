package empleado;

import org.apache.log4j.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        Connection con = conectarBase();
        Statement stmt = crearTabla(con);
        agregarRepetido(stmt);
        actualizarNombrePorId(3,"Roberto", stmt);
        eliminarPorId(5, stmt);
        eliminarUnoPorEmpresa("Google", stmt);
        mostrarTabla(stmt);
        con.close();
        logger.info("Conexión a DB finalizada");
    }

    public static Connection conectarBase(){
        Connection con = null;

        String url = "jdbc:h2:~/test";
        String user = "sa";
        String pass = "";

        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            logger.fatal("El driver no existe", e);
            System.exit(0);
        }

        try {
            con = DriverManager.getConnection(url, user, pass);
            con.setAutoCommit(false);
        } catch (SQLException e) {
            logger.fatal("No es posible conectarse", e);
            System.exit(0);
        }

        logger.info("Conexion a DB correcta");
        return con;
    }

    public static Statement crearTabla(Connection con){
        //Código para crear una tabla. Elimina la tabla si esta ya existe y la
        //vuelve a crear

        Statement stmt = null;
        String createSql = "DROP TABLE IF EXISTS EMPLEADO;\n" +
                "CREATE TABLE EMPLEADO(ID INT PRIMARY KEY AUTO_INCREMENT, NOMBRE VARCHAR(255), EDAD INT, EMPRESA VARCHAR(255), FECHA_INGRESO DATE);\n" +
                "INSERT INTO EMPLEADO (NOMBRE, EDAD, EMPRESA, FECHA_INGRESO) VALUES\n" +
                "('Damian', 42, 'Digital', '2022-11-20'),\n" +
                "('Mark', 46, 'Facebook', '2002-01-15'),\n" +
                "('Rafael', 33, 'Digital', '1999-01-15'),\n" +
                "('Miguel Angel', 36, 'Facebook', '1985-01-15'),\n" +
                "('Donatello', 56, 'Digital', '2010-01-15'),\n" +
                "('Larry', 50, 'Google', '1998-08-20');";
        try {
            stmt = con.createStatement();
            stmt.execute(createSql);
        } catch (Exception e){
            logger.error("El código presenta un problema", e);
        }
        return stmt;
    }

    public static void agregarRepetido(Statement stmt){
/*
        Debemos insertar tres filas con distintos datos, y una de ellas debe tener el ID repetido.
        Tenemos que verificar que nuestro log está mostrando este error —como los ID son primary keys,
        al intentar insertar el mismo id, nos va a dar un error, loguear el error—.
*/
        String addRowSql = "INSERT INTO EMPLEADO VALUES " +
                "(2, 'Damian', 42, 'Digital', '2022-11-20');";
        try {
            stmt.execute(addRowSql);
        } catch (Exception e) {
            logger.error("El código presenta un problema", e);
        }
    }

    public static void actualizarNombrePorId(int id, String nombre, Statement stmt){
/*
        Luego, hay que actualizar a uno de los empleados con un nuevo valor en alguna de las columnas
        y loguear con un objeto debug toda la información del empleado.
*/
        ResultSet rd;
        Empleado e1 = new Empleado();

        String query = "select * from EMPLEADO WHERE ID=" + id + ";";
        try {
            rd = stmt.executeQuery(query);

            //Código para recorrer el resultado de la consulta
            rd.next();
            e1.setId(rd.getInt("ID"));
            e1.setNombre(rd.getString("NOMBRE"));
            e1.setEmpresa(rd.getString("EMPRESA"));
            e1.setEdad(rd.getInt("EDAD"));
            e1.setFechaIngreso(rd.getDate("FECHA_INGRESO").toLocalDate());

            logger.debug("Registro original: " + e1);
        } catch (Exception e) {
            logger.error("Error en la consulta", e);
        }

        String modificar = "UPDATE EMPLEADO " +
                "SET NOMBRE='" + nombre + "' " +
                "WHERE ID=" + id + ";";
        try {
            stmt.execute(modificar);
        } catch (Exception e) {
            logger.error("El código presenta un problema", e);
        }

        query = "select * from EMPLEADO WHERE ID=" + id + ";";
        try {
            rd = stmt.executeQuery(query);

            //Código para recorrer el resultado de la consulta
            rd.next();
            e1.setId(rd.getInt("ID"));
            e1.setNombre(rd.getString("NOMBRE"));
            e1.setEmpresa(rd.getString("EMPRESA"));
            e1.setEdad(rd.getInt("EDAD"));
            e1.setFechaIngreso(rd.getDate("FECHA_INGRESO").toLocalDate());

            logger.debug("Registro modificado: " + e1);
        } catch (Exception e) {
            logger.error("Error en la consulta", e);
        }
    }

    public static void eliminarPorId(int id, Statement stmt){
/*
        Posteriormente, tenemos que eliminar un empleado según el ID y
        loguear como un objeto info toda la información del empleado eliminado.
*/
        Empleado e2 = new Empleado();
        ResultSet rd;

        String queryBorrar = "select * from EMPLEADO WHERE ID=" + id + ";";
        try {
            rd = stmt.executeQuery(queryBorrar);

            String salida = "Se eliminará el siguiente Registro: ";

            //Código para recorrer el resultado de la consulta
            rd.next();
            e2.setId(rd.getInt("ID"));
            e2.setNombre(rd.getString("NOMBRE"));
            e2.setEmpresa(rd.getString("EMPRESA"));
            e2.setEdad(rd.getInt("EDAD"));
            e2.setFechaIngreso(rd.getDate("FECHA_INGRESO").toLocalDate());

        } catch (Exception e) {
            logger.error("Error en la consulta", e);
        }

        String eliminar = "DELETE FROM EMPLEADO WHERE ID=" + id + ";";

        try {
            stmt.execute(eliminar);
            logger.info("Se eliminó el siguiente Registro: " + e2);

        } catch (Exception e) {
            logger.error("El código presenta un problema", e);
        }
    }


    public static void eliminarUnoPorEmpresa(String empresa, Statement stmt){
/*
    Por último, eliminamos otro empleado según otra columna —por ejemplo,
    email— y loguear como un objeto info toda la información del empleado eliminado.
 */
        Empleado e3 = new Empleado();
        ResultSet rd;

        String queryBorrar = "select * from EMPLEADO WHERE EMPRESA='" + empresa + "';";
        try {
            rd = stmt.executeQuery(queryBorrar);

            //Código para recorrer el resultado de la consulta
            rd.next();

            e3.setId(rd.getInt("ID"));
            e3.setNombre(rd.getString("NOMBRE"));
            e3.setEmpresa(rd.getString("EMPRESA"));
            e3.setEdad(rd.getInt("EDAD"));
            e3.setFechaIngreso(rd.getDate("FECHA_INGRESO").toLocalDate());

        } catch (Exception e) {
            logger.error("Error en la consulta", e);
        }

        String eliminar = "DELETE FROM EMPLEADO WHERE EMPRESA='" + empresa + "'";

        try {
            stmt.execute(eliminar);
            logger.info("Se eliminó el siguiente Registro: " + e3);

        } catch (Exception e) {
            logger.error("El código presenta un problema", e);
        }

    }

    public static void mostrarTabla(Statement stmt){
        //Codigo para consultar todos los registros de la tabla EMPLEADO
        List<Empleado> empleados = new ArrayList<>();
        ResultSet rd;

        String sql = "select * from EMPLEADO";
        try {
            rd = stmt.executeQuery(sql);

            //Código para recorrer el resultado de la consulta

            while (rd.next()) {
                Empleado empleado = new Empleado();
                empleado.setId(rd.getInt("ID"));
                empleado.setNombre(rd.getString("NOMBRE"));
                empleado.setEmpresa(rd.getString("EMPRESA"));
                empleado.setEdad(rd.getInt("EDAD"));
                empleado.setFechaIngreso(rd.getDate("FECHA_INGRESO").toLocalDate());

                empleados.add(empleado);
            }

            String salida = "Contenido de la tabla:\n";
            for (Empleado emp : empleados) {
                salida += emp + "\n";
            }

            logger.info(salida);
        } catch (Exception e) {
            logger.error("Error en la consulta", e);
        }
    }

}
