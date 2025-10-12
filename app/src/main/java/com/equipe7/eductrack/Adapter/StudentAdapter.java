package com.equipe7.eductrack.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.equipe7.eductrack.R;
import com.equipe7.eductrack.Utils.StudentModel;

import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<StudentModel> studentList;
    private List<StudentModel> studentListFull; // pour la recherche

    // ✅ Constructeur unique basé sur StudentModel
    public StudentAdapter(List<StudentModel> studentList) {
        this.studentList = studentList;
        this.studentListFull = new ArrayList<>(studentList);
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new StudentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        StudentModel student = studentList.get(position);
        holder.tvLine1.setText("Name: " + student.getName());
        holder.tvLine2.setText(
                "Class: " + student.getStudentClass() +
                        " | School: " + student.getSchool() +
                        " | Code: " + student.getStudentCode()
        );
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    // 🔎 Fonction de recherche
    public void filter(String text) {
        studentList.clear();
        if (text.isEmpty()) {
            studentList.addAll(studentListFull);
        } else {
            text = text.toLowerCase();
            for (StudentModel s : studentListFull) {
                if (s.getName().toLowerCase().contains(text) ||
                        s.getStudentClass().toLowerCase().contains(text) ||
                        s.getSchool().toLowerCase().contains(text) ||
                        s.getStudentCode().toLowerCase().contains(text)) {
                    studentList.add(s);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvLine1, tvLine2;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLine1 = itemView.findViewById(android.R.id.text1);
            tvLine2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
