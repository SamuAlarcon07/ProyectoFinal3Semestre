package co.edu.uniquindio.proyectofinal.model.util;



import javax.crypto.SecretKeyFactory;

import javax.crypto.spec.PBEKeySpec;

import java.security.GeneralSecurityException;

import java.security.MessageDigest;

import java.security.SecureRandom;

import java.util.Base64;



/** Genera y verifica hashes de contraseñas con PBKDF2-HMAC-SHA256. */

public class PasswordHasher {



    private static final int ITERACIONES = 210_000;

    private static final int TAMANO_SAL = 16;

    private static final int TAMANO_CLAVE = 256;

    private final SecureRandom random = new SecureRandom();



    public String hash(String contrasena) {

        validarContrasena(contrasena);

        byte[] sal = new byte[TAMANO_SAL];

        random.nextBytes(sal);

        byte[] resultado = derivar(contrasena.toCharArray(), sal, ITERACIONES);

        return "pbkdf2$" + ITERACIONES + "$"

                + Base64.getEncoder().encodeToString(sal) + "$"

                + Base64.getEncoder().encodeToString(resultado);

    }



    public boolean verificar(String contrasena, String hashAlmacenado) {

        if (contrasena == null || hashAlmacenado == null) return false;

        try {

            String[] partes = hashAlmacenado.split("\\$");

            if (partes.length != 4 || !partes[0].equals("pbkdf2")) return false;

            int iteraciones = Integer.parseInt(partes[1]);

            byte[] sal = Base64.getDecoder().decode(partes[2]);

            byte[] esperado = Base64.getDecoder().decode(partes[3]);

            byte[] calculado = derivar(contrasena.toCharArray(), sal, iteraciones);

            return MessageDigest.isEqual(esperado, calculado);

        } catch (RuntimeException ex) {

            return false;

        }

    }



    private byte[] derivar(char[] contrasena, byte[] sal, int iteraciones) {

        PBEKeySpec especificacion = new PBEKeySpec(contrasena, sal, iteraciones, TAMANO_CLAVE);

        try {

            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")

                    .generateSecret(especificacion).getEncoded();

        } catch (GeneralSecurityException ex) {

            throw new IllegalStateException("No fue posible procesar la contraseña.", ex);

        } finally {

            especificacion.clearPassword();

            java.util.Arrays.fill(contrasena, '\0');

        }

    }



    private void validarContrasena(String contrasena) {

        if (contrasena == null || contrasena.length() < 8) {

            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");

        }

    }

}