package br.unicentro.oficina.service;

import br.unicentro.oficina.exception.ServiceException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/** Copia, localiza e remove as fotos dos veículos (pasta "fotos" ao lado do projeto). */
public class FotoService {

    private static final Path PASTA = Paths.get("fotos");
    private static final long TAMANHO_MAXIMO = 5L * 1024 * 1024; // 5 MB
    private static final Set<String> EXTENSOES = Set.of("png", "jpg", "jpeg", "gif", "bmp");

    /** Copia a imagem para a pasta do sistema e devolve o caminho relativo a ser gravado no banco. */
    public String salvar(File origem) throws ServiceException {
        if (origem == null || !origem.isFile()) {
            throw new ServiceException("Arquivo de imagem não encontrado.");
        }
        String extensao = extensao(origem.getName());
        if (!EXTENSOES.contains(extensao)) {
            throw new ServiceException("Formato de imagem não suportado. Use PNG, JPG, GIF ou BMP.");
        }
        if (origem.length() > TAMANHO_MAXIMO) {
            throw new ServiceException("A imagem excede o limite de 5 MB.");
        }
        try {
            Files.createDirectories(PASTA);
            Path destino = PASTA.resolve(UUID.randomUUID() + "." + extensao);
            Files.copy(origem.toPath(), destino);
            return destino.toString().replace('\\', '/');
        } catch (IOException e) {
            throw new ServiceException("Não foi possível salvar a foto: " + e.getMessage(), e);
        }
    }

    /** Remove a foto, apenas se estiver dentro da pasta do sistema. Falhas são ignoradas. */
    public void excluir(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            return;
        }
        try {
            Path arquivo = Paths.get(caminho).normalize();
            if (arquivo.startsWith(PASTA)) {
                Files.deleteIfExists(arquivo);
            }
        } catch (IOException ignorada) {
            // Foto órfã não compromete o sistema.
        }
    }

    /** Devolve o arquivo da foto ou null se não existir (útil para o Controller exibir a imagem). */
    public File obter(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            return null;
        }
        File arquivo = new File(caminho);
        return arquivo.isFile() ? arquivo : null;
    }

    private String extensao(String nome) {
        int ponto = nome.lastIndexOf('.');
        return ponto < 0 ? "" : nome.substring(ponto + 1).toLowerCase();
    }
}
