package com.erp.monitor;

import java.text.DecimalFormat;
import java.util.function.Supplier;

public class MonitorPerformace {
    private static final Runtime runtime = Runtime.getRuntime();
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");

    /**
     * Mede o tempo de execução de uma operação
     */
    public static <T> ResultadoPerformance<T> medirTempo(String operacao, Supplier<T> funcao) {
        long inicio = System.nanoTime();
        T resultado = funcao.get();
        long fim = System.nanoTime();

        long tempoNano = fim - inicio;
        double tempoMs = tempoNano / 1_000_000.0;

        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║  MEDIÇÃO DE TEMPO                      ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Operação: " + operacao);
        System.out.println("║ Tempo: " + df.format(tempoMs) + " ms");
        System.out.println("║ Tempo: " + df.format(tempoNano / 1000.0) + " μs");
        System.out.println("╚════════════════════════════════════════╝\n");

        return new ResultadoPerformance<>(resultado, tempoMs);
    }

    /**
     * Mede tempo e memória de uma operação
     */
    public static <T> ResultadoPerformance<T> medirCompleto(String operacao, Supplier<T> funcao) {
        // Força garbage collection antes
        System.gc();
        try { Thread.sleep(100); } catch (InterruptedException e) {}

        long memoriaAntes = runtime.totalMemory() - runtime.freeMemory();
        long inicio = System.nanoTime();

        T resultado = funcao.get();

        long fim = System.nanoTime();
        long memoriaDepois = runtime.totalMemory() - runtime.freeMemory();

        long tempoNano = fim - inicio;
        double tempoMs = tempoNano / 1_000_000.0;
        long memoriaUsada = memoriaDepois - memoriaAntes;
        double memoriaMB = memoriaUsada / (1024.0 * 1024.0);

        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║  ANÁLISE COMPLETA DE PERFORMANCE       ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Operação: " + operacao);
        System.out.println("║ ");
        System.out.println("║ TEMPO:");
        System.out.println("║   • " + df.format(tempoMs) + " ms");
        System.out.println("║   • " + df.format(tempoNano / 1000.0) + " μs");
        System.out.println("║ ");
        System.out.println("║ MEMÓRIA:");
        System.out.println("║   • Usada: " + df.format(memoriaMB) + " MB");
        System.out.println("║   • Bytes: " + String.format("%,d", memoriaUsada) + " bytes");
        System.out.println("║   • Antes: " + df.format(memoriaAntes / (1024.0 * 1024.0)) + " MB");
        System.out.println("║   • Depois: " + df.format(memoriaDepois / (1024.0 * 1024.0)) + " MB");
        System.out.println("╚════════════════════════════════════════╝\n");

        return new ResultadoPerformance<>(resultado, tempoMs, memoriaMB);
    }

    /**
     * Exibe estatísticas de memória atual
     */
    public static void exibirMemoria() {
        long memoriaTotal = runtime.totalMemory();
        long memoriaLivre = runtime.freeMemory();
        long memoriaUsada = memoriaTotal - memoriaLivre;
        long memoriaMaxima = runtime.maxMemory();

        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║  MEMÓRIA DO SISTEMA                    ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Total: " + df.format(memoriaTotal / (1024.0 * 1024.0)) + " MB");
        System.out.println("║ Usada: " + df.format(memoriaUsada / (1024.0 * 1024.0)) + " MB");
        System.out.println("║ Livre: " + df.format(memoriaLivre / (1024.0 * 1024.0)) + " MB");
        System.out.println("║ Máxima: " + df.format(memoriaMaxima / (1024.0 * 1024.0)) + " MB");
        System.out.println("║ Uso: " + df.format((memoriaUsada * 100.0) / memoriaTotal) + "%");
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    /**
     * Compara performance entre duas operações
     */
    public static <T> void comparar(String nome1, Supplier<T> op1, String nome2, Supplier<T> op2) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║  COMPARAÇÃO DE PERFORMANCE             ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        ResultadoPerformance<T> r1 = medirCompleto(nome1, op1);
        ResultadoPerformance<T> r2 = medirCompleto(nome2, op2);

        double diferencaTempo = ((r2.getTempoMs() - r1.getTempoMs()) / r1.getTempoMs()) * 100;
        double diferencaMemoria = r2.getMemoriaMB() - r1.getMemoriaMB();

        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║  RESULTADO DA COMPARAÇÃO               ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ " + nome2 + " vs " + nome1 + ":");
        System.out.println("║ ");
        System.out.println("║ Tempo: " + (diferencaTempo > 0 ? "+" : "") +
                df.format(diferencaTempo) + "%");
        System.out.println("║ Memória: " + (diferencaMemoria > 0 ? "+" : "") +
                df.format(diferencaMemoria) + " MB");
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    /**
     * Classe para armazenar resultados
     */
    public static class ResultadoPerformance<T> {
        private final T resultado;
        private final double tempoMs;
        private final double memoriaMB;

        public ResultadoPerformance(T resultado, double tempoMs) {
            this(resultado, tempoMs, 0);
        }

        public ResultadoPerformance(T resultado, double tempoMs, double memoriaMB) {
            this.resultado = resultado;
            this.tempoMs = tempoMs;
            this.memoriaMB = memoriaMB;
        }

        public T getResultado() { return resultado; }
        public double getTempoMs() { return tempoMs; }
        public double getMemoriaMB() { return memoriaMB; }
    }
}
