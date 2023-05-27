package Calculadora;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class SoloCrossfit {

    private JFrame frame;
    private JTextField nombreField;
    private JTextField pesoField;
    private JTextField horasExtraField;
    private JCheckBox competidorCheckBox;
    private JTextArea resultadoTextArea;
    private JComboBox<String> planComboBox;
    private JComboBox<String> categoriaComboBox;
    private ArrayList<User> usuarios;

    private static final String[] NUEVOS_PLANES = {
    	    "Novato (2 sesiones por semana) - Tarifa semanal (25.00 Pounds)",
    	    "Intermedio (3 sesiones por semana) - Tarifa semanal (30.00 Pounds)",
    	    "Élite (5 sesiones por semana) - Tarifa semanal (35.00 Pounds)",
    	    "Entrada Privada - Tarifa por hora",
    	    "Entrada de Competidor - Por competición (22.00 Pounds)"
    	};

    	private static final String[] NUEVAS_CATEGORIAS = {
    	    "Peso Pesado",
    	    "Peso Semipesado",
    	    "Peso Medio",
    	    "Peso Semimedio",
    	    "Peso Ligero",
    	    "Peso Semiligero"
    	};


    private Connection connection;

    public SoloCrossfit() {
    	ConexionBD();

        frame = new JFrame("SoloCrossFit");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JPanel formularioPanel = new JPanel();
        formularioPanel.setLayout(new GridLayout(10, 5));
        formularioPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel calcularPanel = new JPanel();
        calcularPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        calcularPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel resultadoPanel = new JPanel();
        resultadoPanel.setLayout(new BorderLayout());
        resultadoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel nombreLabel = new JLabel("Nombre:");
        nombreField = new JTextField();

        JLabel planLabel = new JLabel("Plan de entrenamiento:");
        planComboBox = new JComboBox<>(NUEVOS_PLANES);

        JLabel pesoLabel = new JLabel("Peso actual (kg):");
        pesoField = new JTextField();

        JLabel categoriaLabel = new JLabel("Categoría:");
        categoriaComboBox = new JComboBox<>(NUEVAS_CATEGORIAS);
        categoriaComboBox.setEnabled(false);

        JLabel horasExtraLabel = new JLabel("Horas extra este mes:");
        horasExtraField = new JTextField();

        competidorCheckBox = new JCheckBox("¿Es competidor?");
        competidorCheckBox.setEnabled(false);

        JButton botonCalcular = new JButton("Calcular");
        

        resultadoTextArea = new JTextArea(10, 70);
        resultadoTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultadoTextArea);

        formularioPanel.add(nombreLabel);
        formularioPanel.add(nombreField);

        formularioPanel.add(planLabel);
        formularioPanel.add(planComboBox);

        formularioPanel.add(pesoLabel);
        formularioPanel.add(pesoField);

        formularioPanel.add(categoriaLabel);
        formularioPanel.add(categoriaComboBox);

        formularioPanel.add(horasExtraLabel);
        formularioPanel.add(horasExtraField);

        formularioPanel.add(competidorCheckBox);

        calcularPanel.add(botonCalcular);

        resultadoPanel.add(new JLabel("Resultados:"), BorderLayout.NORTH);
        resultadoPanel.add(scrollPane, BorderLayout.CENTER);
        
        resultadoPanel.add(new JLabel("Usuarios Registrados:"), BorderLayout.NORTH);
        resultadoPanel.add(scrollPane, BorderLayout.CENTER);

        frame.add(formularioPanel, BorderLayout.NORTH);
        frame.add(calcularPanel, BorderLayout.CENTER);
        frame.add(resultadoPanel, BorderLayout.SOUTH);
        
        usuarios = new ArrayList<>();
        
        usuarios.add(new User("Rodrigo Pozo Sánchez", NUEVOS_PLANES[0], 80.0, "", 0, false));
        usuarios.add(new User("Rafael Gómez Pérez", NUEVOS_PLANES[0], 75.0, "", 2, false));
        usuarios.add(new User("Carlos López Rodríguez", NUEVOS_PLANES[1], 70.0, "", 3, false));
        usuarios.add(new User("Ana Martínez García", NUEVOS_PLANES[2], 65.0, "", 0, false));
        usuarios.add(new User("Laura Torres Fernández", NUEVOS_PLANES[3], 60.0, "", 1, false));

        devolverUsusario();

        planComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarCompetidorCheckBox();
            }
        });

        botonCalcular.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calcularCosto();
            }
        });

        frame.setVisible(true);
    }
    
   
    private void ConexionBD() {
        String url = "jdbc:mysql://localhost:3306/SoloCrossfit";
        String user = "root";
        String password = "";

        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Conexión Establecida");
        } catch (SQLException e) {
            System.out.println("FALLO AL CONECTAR CON LA BASE DE DATOS");
            e.printStackTrace();
        }
    }

    private void devolverUsusario() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM usuarios");

            while (resultSet.next()) {
                String nombre = resultSet.getString("Nombre");
                String plan = resultSet.getString("PlanEntrenamiento");
                double peso = resultSet.getDouble("PesoActual");
                String categoria = resultSet.getString("Categoria");
                int horasExtra = resultSet.getInt("HorasExtra");
                boolean competidor = resultSet.getBoolean("Competidor");

                User user = new User(nombre, plan, peso, categoria, horasExtra, competidor);
                usuarios.add(user);
            }

            displayUsers();
        } catch (SQLException e) {
            System.out.println("No se ha podido obtener los usuarios de la base de datos");
            e.printStackTrace();
        }
    }

    private void actualizarCompetidorCheckBox() {
        String plan = (String) planComboBox.getSelectedItem();
        boolean seleccionPermitida = plan.equals(NUEVOS_PLANES[1]) || plan.equals(NUEVOS_PLANES[2]);
        competidorCheckBox.setEnabled(seleccionPermitida);
    }

    private void calcularCosto() {
        String nombre = nombreField.getText();
        String plan = (String) planComboBox.getSelectedItem();
        double peso = Double.parseDouble(pesoField.getText());
        int horasExtra = Integer.parseInt(horasExtraField.getText());
        boolean esCompetidor = competidorCheckBox.isSelected();

        if (horasExtra > 5) {
            JOptionPane.showMessageDialog(frame, "No se pueden introducir más de 5 horas extra.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double costoSemanal = obtenerCostoSemanal(plan);
        double costoTotal = calcularCostoTotal(costoSemanal, horasExtra, esCompetidor);
        String categoria = obtenerCategoria(peso);

        DecimalFormat df = new DecimalFormat("#.00");
        String resultado = "Nombre: " + nombre + "\n";
        resultado += "Plan: " + plan + "\n";
        resultado += "Peso: " + peso + " kg" + "\n";
        resultado += "Categoría: " + categoria + "\n";
        resultado += "Horas extra: " + horasExtra + "\n";
        resultado += "Competidor: " + (esCompetidor ? "Sí" : "No") + "\n";
        resultado += "Costo total: " + df.format(costoTotal) + " Pounds";

        resultadoTextArea.setText(resultado);

        User usuario = new User(nombre, plan, peso, categoria, horasExtra, esCompetidor);
        usuarios.add(usuario);
        insertUserIntoDatabase(usuario);
        displayUsers();
    }

    private double obtenerCostoSemanal(String plan) {
        double costoSemanal = 0;

        if (plan.equals(NUEVOS_PLANES[0])) {
            costoSemanal = 25.0;
        } else if (plan.equals(NUEVOS_PLANES[1])) {
            costoSemanal = 30.0;
        } else if (plan.equals(NUEVOS_PLANES[2])) {
            costoSemanal = 35.0;
        } else if (plan.equals(NUEVOS_PLANES[3])) {
           
            costoSemanal = 9.50;
        } else if (plan.equals(NUEVOS_PLANES[4])) {
            
            costoSemanal = 22.0;
        }

        return costoSemanal;
    }

    private double calcularCostoTotal(double costoSemanal, int horasExtra, boolean esCompetidor) {
        double costoTotal = costoSemanal;

        if (horasExtra > 0) {
            costoTotal += horasExtra * 3.0;
        }

        if (esCompetidor) {
            costoTotal += 22.0;
        }

        return costoTotal;
    }

    private String obtenerCategoria(double peso) {
        if (peso >= 100.0) {
            return NUEVAS_CATEGORIAS[0];
        } else if (peso >= 90.0) {
            return NUEVAS_CATEGORIAS[1];
        } else if (peso >= 81.0) {
            return NUEVAS_CATEGORIAS[2];
        } else if (peso >= 73.0) {
            return NUEVAS_CATEGORIAS[3];
        } else if (peso >= 66.0) {
            return NUEVAS_CATEGORIAS[4];
        } else {
            return NUEVAS_CATEGORIAS[5];
        }
    }

    private void displayUsers() {
        StringBuilder sb = new StringBuilder();

        for (User user : usuarios) {
            sb.append("Nombre: ").append(user.getNombre()).append(", ");
            sb.append("Plan: ").append(user.getPlan()).append(", ");
            sb.append("Peso: ").append(user.getPeso()).append(" kg").append(", ");
            sb.append("Categoría: ").append(user.getCategoria()).append(", ");
            sb.append("Horas extra: ").append(user.getHorasExtra()).append(", ");
            sb.append("Competidor: ").append(user.isCompetidor() ? "Sí" : "No").append("\n");
        }

        resultadoTextArea.setText(sb.toString());
    }

    private void insertUserIntoDatabase(User user) {
        String nombre = user.getNombre();
        String plan = user.getPlan();
        double peso = user.getPeso();
        String categoria = user.getCategoria();
        int horasExtra = user.getHorasExtra();
        boolean competidor = user.isCompetidor();

        String query = "INSERT INTO usuarios (Nombre, PlanEntrenamiento, PesoActual, Categoria, HorasExtra, Competidor) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, nombre);
            statement.setString(2, plan);
            statement.setDouble(3, peso);
            statement.setString(4, categoria);
            statement.setInt(5, horasExtra);
            statement.setBoolean(6, competidor);

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("Se ha insertado un nuevo usuario");
            } else {
                System.out.println("No se ha podido insertar el usuarios en la base de datos");
            }
        } catch (SQLException e) {
            System.out.println("FALLO TOTAL");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SoloCrossfit();
    }

    public static String[] getNuevosPlanes() {
		return NUEVOS_PLANES;
	}

	public static String[] getNuevasCategorias() {
		return NUEVAS_CATEGORIAS;
	}

	private class User {
        private String nombre;
        private String plan;
        private double peso;
        private String categoria;
        private int horasExtra;
        private boolean competidor;

        public User(String nombre, String plan, double peso, String categoria, int horasExtra, boolean competidor) {
            this.nombre = nombre;
            this.plan = plan;
            this.peso = peso;
            this.categoria = categoria;
            this.horasExtra = horasExtra;
            this.competidor = competidor;
        }

        public String getNombre() {
            return nombre;
        }

        public String getPlan() {
            return plan;
        }

        public double getPeso() {
            return peso;
        }

        public String getCategoria() {
            return categoria;
        }

        public int getHorasExtra() {
            return horasExtra;
        }

        public boolean isCompetidor() {
            return competidor;
        }
    }
}
