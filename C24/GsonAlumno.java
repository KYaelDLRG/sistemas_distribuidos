import com.google.gson.Gson;
import java.util.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class GsonAlumno {
    public static void main(String[] args) {
        // Convertir objeto Alumno a JSON
        Gson gson = new Gson();
        Alumno alumno = new Alumno(1, "Carlos", "Pérez", "Castro", 8.7);
        String jsonAlumno = gson.toJson(alumno);
        System.out.println(jsonAlumno);

        // Convertir JSON a objeto Alumno
        Alumno alumno2 = gson.fromJson(jsonAlumno, Alumno.class);
        System.out.println(alumno2.getNombre());

        // Convertir lista de objetos a JSON
        List<Alumno> alumnos = new ArrayList<>();
        alumnos.add(new Alumno(1, "Ana", "Carmona", "Trujillo", 9.1));
        alumnos.add(new Alumno(2, "Luis", "Cuellar", "Paz", 8.5));
        alumnos.add(new Alumno(3, "Carlos", "Pérez", "Castro", 8.7));
        String jsonAlumnos = gson.toJson(alumnos);
        System.out.println(jsonAlumnos);

        // Convertir JSON a lista de objetos
        Type tipoLista = new TypeToken<List<Alumno>>(){}.getType();
        List<Alumno> alumnos2 = gson.fromJson(jsonAlumnos, tipoLista);
        for (Alumno a : alumnos2) {
            System.out.println(a.getNombre());
        }
    }
}
