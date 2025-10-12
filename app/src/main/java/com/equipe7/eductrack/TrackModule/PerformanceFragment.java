package com.equipe7.eductrack.TrackModule;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.equipe7.eductrack.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PerformanceFragment extends Fragment {

    private FirebaseFirestore db;
    private ProgressBar overallProgress;
    private TextView tvOverallPercent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_performance, container, false);

        // Initialisation Firestore
        db = FirebaseFirestore.getInstance();

        // ProgressBar + moyenne générale
        overallProgress = v.findViewById(R.id.overallProgress);
        tvOverallPercent = v.findViewById(R.id.tvOverallPercent);

        // Lier toutes les matières automatiquement
        bindAllSubjects(v);

        // Lier la moyenne générale
        bindOverall();

        return v;
    }

    /**
     * Lier toutes les matières via une Map pour éviter la répétition
     */
    private void bindAllSubjects(View v) {
        Map<String, Pair<Integer, String>> subjects = new HashMap<>();
        subjects.put("mathematics", new Pair<>(R.id.etMath, "Mathematics"));
        subjects.put("french", new Pair<>(R.id.etFrench, "French"));
        subjects.put("english", new Pair<>(R.id.etEnglish, "English"));
        subjects.put("set", new Pair<>(R.id.etSet, "SET"));
        subjects.put("social_studies", new Pair<>(R.id.etSocial, "Social Studies"));
        subjects.put("kinyarwanda", new Pair<>(R.id.etKinyarwanda, "Kinyarwanda"));

        for (Map.Entry<String, Pair<Integer, String>> entry : subjects.entrySet()) {
            TextView target = v.findViewById(entry.getValue().first); // ici ce sera un TextInputEditText
            bindSubject(entry.getKey(), target, entry.getValue().second);
        }
    }


    /**
     * Récupère et affiche la moyenne d’une matière
     */
    private void bindSubject(String docId, TextView target, String label) {
        db.collection("performances")
                .document(docId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        target.setText(label + " : --%");
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        Long avg = snapshot.getLong("average");
                        target.setText(label + " : " + (avg != null ? avg + "%" : "--%"));
                    } else {
                        target.setText(label + " : --%");
                    }
                });
    }

    /**
     * Récupère et affiche la moyenne générale avec animation
     */
    private void bindOverall() {
        db.collection("performances")
                .document("overall")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        tvOverallPercent.setText("Overall Average: --%");
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        Long avg = snapshot.getLong("average");
                        if (avg != null) {
                            int val = avg.intValue();

                            // Animation fluide de la ProgressBar
                            ObjectAnimator animation = ObjectAnimator.ofInt(overallProgress, "progress", 0, val);
                            animation.setDuration(1000);
                            animation.start();

                            tvOverallPercent.setText("Overall Average: " + val + "%");
                        } else {
                            tvOverallPercent.setText("Overall Average: --%");
                        }
                    } else {
                        tvOverallPercent.setText("Overall Average: --%");
                    }
                });
    }
}
