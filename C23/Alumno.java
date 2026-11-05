/**
 * Clase Alumno para el servicio REST micro-SAES.
 * Representa la entidad Alumno con identificador único y atributos académicos.
 * Sistemas Distribuidos - ESCOM IPN
 */
public class Alumno {
    int id;
    String nombre;
    String apellido1;
    String apellido2;
    double promedio;

    public Alumno() {
    }

    public Alumno(int id, String nombre, String apellido1, String apellido2, double promedio) {
        this.id = id;
        this.nombre = nombre;
        this.apellido1 = apellido1;
        this.apellido2 = apellido2;
        this.promedio = promedio;
    }

    public int getId() {
        return id;
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

    public String getApellido1() {
        return apellido1;
    }

    public void setApellido1(String apellido1) {
        this.apellido1 = apellido1;
    }

    public String getApellido2() {
        return apellido2;
    }

    public void setApellido2(String apellido2) {
        this.apellido2 = apellido2;
    }

    public double getPromedio() {
        return promedio;
    }

    public void setPromedio(double promedio) {
        this.promedio = promedio;
    }

    @Override
    public String toString() {
        return "ID: " + id + "\tNombre: " + nombre + "\tApellido 1: " + apellido1 + "\tApellido 2: " + apellido2 + "\tPromedio: " + promedio + "\n";
    }
}
