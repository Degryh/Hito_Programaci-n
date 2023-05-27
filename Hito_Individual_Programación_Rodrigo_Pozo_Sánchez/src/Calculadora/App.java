package Calculadora;

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class App {

    private JFrame frame;
    private JTextField nombreField;
    private JTextField pesoField;
    private JTextField horasExtraField;
    private JCheckBox competidorCheckBox;
    private JTextArea usuariosTextArea;
    private JTextArea resultadoTextArea;
    private JComboBox<String> planComboBox;
    private JComboBox<String> categoriaComboBox;
    private ArrayList<User> usuarios;

    private static final String[] PLANES = {
        "Principiante (2 sesiones por semana) - Tarifa semanal (25.00 Pounds)",
        "Intermedio (3 sesiones por semana) - Tarifa semanal (30.00 Pounds)",
        "Elite (5 Sesiones por semana) - Tarifa semanal (35.00 Pounds)",
        "Entrada Privada - Tarifa por hora",
        "Entrada de Competidor - Por competición (22.00 Pounds)"
    };

    private static final String[] CATEGORIAS = {
        "Peso Pesado",
        "Peso semipesado",
        "Peso medio",
        "Peso semi medio",
        "Peso ligero",
        "Peso semi ligero"
    };

    public App() {
        frame = new JFrame("SoloCrossfit");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(2480, 900);
        frame.setLayout(new BorderLayout());

        JPanel formularioPanel = new JPanel();
        formularioPanel.setLayout(new GridLayout(10, 5));
        formularioPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel calcularPanel = new JPanel();
        calcularPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        calcularPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel usuariosPanel = new JPanel();
        usuariosPanel.setLayout(new BorderLayout());
        usuariosPanel.setBorder(BorderFactory.createEmptyBorder(50, 550, 0, 0));

        JLabel nombreLabel = new JLabel("Nombre:");
        nombreField = new JTextField();

        JLabel planLabel = new JLabel("Plan de entrenamiento:");
        planComboBox = new JComboBox<>(PLANES);

        JLabel pesoLabel = new JLabel("Peso actual (kg):");
        pesoField = new JTextField();

        JLabel categoriaLabel = new JLabel("Categoría:");
        categoriaComboBox = new JComboBox<>(CATEGORIAS);
        categoriaComboBox.setEnabled(false);

        JLabel horasExtraLabel = new JLabel("Horas extra este mes:");
        horasExtraField = new JTextField();

        competidorCheckBox = new JCheckBox("¿Es competidor?");
        competidorCheckBox.setEnabled(false);

        JButton botonCalcular = new JButton("Calcular");

        usuariosTextArea = new JTextArea(10, 70);
        usuariosTextArea.setEditable(false);
        JScrollPane usuariosScrollPane = new JScrollPane(usuariosTextArea);


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

        usuariosPanel.add(usuariosScrollPane, BorderLayout.CENTER);

        frame.add(formularioPanel, BorderLayout.NORTH);
        frame.add(calcularPanel, BorderLayout.CENTER);
        frame.add(usuariosPanel, BorderLayout.WEST);

        usuarios = new ArrayList<>();

        botonCalcular.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calcularCosto();
            }
        });
    }

    private void calcularCosto() {
        String nombre = nombreField.getText();
        String plan = (String) planComboBox.getSelectedItem();
        double peso = Double.parseDouble(pesoField.getText());
        int horasExtra = Integer.parseInt(horasExtraField.getText());
        boolean esCompetidor = competidorCheckBox.isSelected();

        String categoria = seleccionarCategoria(peso);

        categoriaComboBox.setSelectedItem(categoria);

        User usuario = new User(nombre, plan, peso, categoria, horasExtra, esCompetidor);
        usuarios.add(usuario);
        displayUsers();

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/crossfitapp", "root", "");

            String sql = "INSERT INTO usuarios (Nombre, PlanEntrenamiento, PesoActual, Categoria, HorasExtra, Competidor) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, nombre);
            statement.setString(2, plan);
            statement.setDouble(3, peso);
            statement.setString(4, categoria);
            statement.setInt(5, horasExtra);
            statement.setBoolean(6, esCompetidor);
            statement.executeUpdate();

            statement.close();
            connection.close();

            System.out.println("Usuario insertado en la base de datos correctamente.");
        } catch (SQLException e) {
            System.out.println("Error al insertar el usuario en la base de datos: " + e.getMessage());
        }
    }
    
    
 
	private void actualizarCompetidorCheckBox() {
        String plan = (String) planComboBox.getSelectedItem();
        boolean seleccionPermitida = plan.equals(PLANES[1]) || plan.equals(PLANES[2]);
        competidorCheckBox.setEnabled(seleccionPermitida);
    }

    private String seleccionarCategoria(double peso) {
        if (peso > 90) {
            return "Peso Pesado";
        } else if (peso > 80) {
            return "Peso semipesado";
        } else if (peso > 70) {
            return "Peso medio";
        } else if (peso > 60) {
            return "Peso semi medio";
        } else if (peso > 50) {
            return "Peso ligero";
        } else {
            return "Peso semi ligero";
        }
    }

    private void displayUsers() {
        usuariosTextArea.setText("");
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/crossfitapp", "root", "");

            String sql = "SELECT * FROM usuarios";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                String nombre = resultSet.getString("Nombre");
                String plan = resultSet.getString("PlanEntrenamiento");
                double peso = resultSet.getDouble("PesoActual");
                String categoria = resultSet.getString("Categoria");
                int horasExtra = resultSet.getInt("HorasExtra");
                boolean esCompetidor = resultSet.getBoolean("Competidor");

                User user = new User(nombre, plan, peso, categoria, horasExtra, esCompetidor);
                usuarios.add(user);
                usuariosTextArea.append(user.toString() + "\n");
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println("Error al obtener los usuarios de la base de datos: " + e.getMessage());
        }
    }
    
    

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                App app = new App();
                app.show();
            }
        });
    }

    public JTextArea getResultadoTextArea() {
		return resultadoTextArea;
	}

	public void setResultadoTextArea(JTextArea resultadoTextArea) {
		this.resultadoTextArea = resultadoTextArea;
	}

	private class User {
        private String nombre;
        private String plan;
        private double peso;
        private String categoria;
        private int horasExtra;
        private boolean esCompetidor;

        public User(String nombre, String plan, double peso, String categoria, int horasExtra, boolean esCompetidor) {
            this.nombre = nombre;
            this.plan = plan;
            this.peso = peso;
            this.categoria = categoria;
            this.horasExtra = horasExtra;
            this.esCompetidor = esCompetidor;
        }

        @Override
        public String toString() {
            return "Nombre: " + nombre + ", Plan: " + plan + ", Peso: " + peso + ", Categoría: " + categoria +
                    ", Horas Extra: " + horasExtra + ", Es Competidor: " + esCompetidor;
        }
    }
}
