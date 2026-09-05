package com.hfad.stopwatch;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class StopwatchActivity extends Activity {

    // Tiempo total transcurrido en segundos
    private int seconds = 0;
    // ¿Está corriendo el cronómetro?
    private boolean running;
    // ¿Estuvo corriendo alguna vez? (útil para lógica futura, opcional)
    private boolean wasRunning;

    // --- Control de vueltas ---
    private static final int MAX_LAPS = 5;
    private int lapCount = 0;
    private int lastLapSeconds = 0;                    // segundo en que terminó la última vuelta
    private final int[] lapTimes = new int[MAX_LAPS];  // tiempo parcial de cada vuelta

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);

        if (savedInstanceState != null) {
            seconds = savedInstanceState.getInt("seconds");
            running = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
            lapCount = savedInstanceState.getInt("lapCount");
            lastLapSeconds = savedInstanceState.getInt("lastLapSeconds");
            int[] savedLaps = savedInstanceState.getIntArray("lapTimes");
            if (savedLaps != null) {
                System.arraycopy(savedLaps, 0, lapTimes, 0, savedLaps.length);
            }
        }

        runTimer();
        updateLapsView();
        updateLapButtonState();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("seconds", seconds);
        savedInstanceState.putBoolean("running", running);
        savedInstanceState.putBoolean("wasRunning", wasRunning);
        savedInstanceState.putInt("lapCount", lapCount);
        savedInstanceState.putInt("lastLapSeconds", lastLapSeconds);
        savedInstanceState.putIntArray("lapTimes", lapTimes);
    }

    // Inicia el cronómetro
    public void onClickStart(View view) {
        running = true;
        wasRunning = true;
    }

    // Detiene el cronómetro
    public void onClickStop(View view) {
        running = false;
    }

    // Reinicia todo, incluidas las vueltas
    public void onClickReset(View view) {
        running = false;
        wasRunning = false;
        seconds = 0;
        lapCount = 0;
        lastLapSeconds = 0;
        for (int i = 0; i < lapTimes.length; i++) {
            lapTimes[i] = 0;
        }
        updateLapsView();
        updateLapButtonState();
    }

    // Registra una vuelta
    public void onClickLap(View view) {
        if (!running || lapCount >= MAX_LAPS) {
            return; // no registrar vueltas si está detenido o ya completó 5
        }

        int lapTime = seconds - lastLapSeconds; // tiempo parcial de esta vuelta
        lapTimes[lapCount] = lapTime;
        lastLapSeconds = seconds;
        lapCount++;

        updateLapsView();
        updateLapButtonState();

        if (lapCount == MAX_LAPS) {
            running = false; // se detiene automáticamente al completar las 5 vueltas
            showFinalReport();
        }
    }

    // Habilita/deshabilita el botón Lap según corresponda
    private void updateLapButtonState() {
        Button lapButton = findViewById(R.id.lap_button);
        lapButton.setEnabled(lapCount < MAX_LAPS);
    }

    // Formatea segundos como h:mm:ss
    private String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int secs = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
    }

    // Actualiza el TextView con la lista de vueltas registradas
    private void updateLapsView() {
        TextView lapsView = findViewById(R.id.laps_view);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lapCount; i++) {
            sb.append(String.format(Locale.getDefault(),
                    "Vuelta %d: %s%n", i + 1, formatTime(lapTimes[i])));
        }
        lapsView.setText(sb.toString());
    }

    // Muestra el reporte final una vez completadas las 5 vueltas
    private void showFinalReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("¡5 vueltas completadas!\n\n");
        int total = 0;
        for (int i = 0; i < lapTimes.length; i++) {
            sb.append(String.format(Locale.getDefault(),
                    "Vuelta %d: %s%n", i + 1, formatTime(lapTimes[i])));
            total += lapTimes[i];
        }
        sb.append(String.format(Locale.getDefault(),
                "%nTiempo total: %s", formatTime(total)));

        TextView lapsView = findViewById(R.id.laps_view);
        lapsView.setText(sb.toString());
    }

    // Actualiza el tiempo transcurrido cada segundo
    private void runTimer() {
        final TextView timeView = findViewById(R.id.time_view);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                timeView.setText(formatTime(seconds));
                if (running) {
                    seconds++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }
}