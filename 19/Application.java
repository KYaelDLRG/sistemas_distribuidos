/*
 *  MIT License
 *
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Clase Application.
 * 
 * Explicación según el video:
 * 1. Comienza por definir las cadenas correspondientes a las direcciones de los endpoints
 *    para los dos servidores (puerto 8081 y 8082 en el endpoint /searchtoken).
 * 2. Instancia un objeto Aggregator.
 * 3. Define la lista de tareas a enviar hacia los servidores trabajadores.
 * 4. Invoca el método sendTasksToWorkers pasando la lista de trabajadores y la lista de tareas.
 * 5. Recibe los resultados agregados e imprime el número de apariciones de cada token.
 */
public class Application {
    private static final String WORKER_ADDRESS_1 = "http://localhost:8081/searchtoken";
    private static final String WORKER_ADDRESS_2 = "http://localhost:8082/searchtoken";

    public static void main(String[] args) {
        Aggregator aggregator = new Aggregator();

        // Lista de las cinco tareas solicitadas en el ejercicio
        List<String> listaTareas = new ArrayList<String>();
        listaTareas.add("1757600,IPN");
        listaTareas.add("17576,SAL");
        listaTareas.add("70000,MAS");
        listaTareas.add("1757600,PEZ");
        listaTareas.add("175700,SOL");

        // Impresión inicial de las tareas a resolver
        System.out.println("Las tareas a resolver son las siguientes:");
        for (int i = 0; i < listaTareas.size(); i++) {
            System.out.println("Tarea " + i + " :" + listaTareas.get(i));
        }
        System.out.println();

        // Envío distribuido de tareas hacia los dos servidores trabajadores
        List<String> results = aggregator.sendTasksToWorkers(
                Arrays.asList(WORKER_ADDRESS_1, WORKER_ADDRESS_2),
                listaTareas);

        // Impresión de resultados con una línea de separación entre cada resultado
        System.out.println();
        for (int i = 0; i < listaTareas.size(); i++) {
            System.out.println("Para la tarea " + listaTareas.get(i) + " El numero de apariciones es " + results.get(i));
            System.out.println();
        }
    }
}
