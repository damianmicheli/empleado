package empleado;

import java.time.LocalDate;

public class Empleado {

    private int id, edad;
    private String nombre, empresa;
    private LocalDate fechaIngreso;


    @Override
    public String toString() {
        return "Empleado {" +
                "id: " + id +
                ", Nombre: '" + nombre + '\'' +
                ", Edad: " + edad +
                ", Empresa: '" + empresa + '\'' +
                ", Fecha de Ingreso: " + fechaIngreso +
                '}';
    }

    public int getId() {
        return id;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }
}
