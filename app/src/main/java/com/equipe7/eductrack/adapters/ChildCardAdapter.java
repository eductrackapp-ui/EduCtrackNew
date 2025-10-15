package com.equipe7.eductrack.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.equipe7.eductrack.Activity.ChildDetailActivity;
import com.equipe7.eductrack.R;
import com.equipe7.eductrack.models.Child;
import java.util.List;

public class ChildCardAdapter extends RecyclerView.Adapter<ChildCardAdapter.ChildViewHolder> {

    private Context context;
    private List<Child> children;
    private String[] gradientColors = {
        "#667eea,#764ba2", // Purple gradient
        "#f093fb,#f5576c", // Pink gradient  
        "#4facfe,#00f2fe", // Blue gradient
        "#43e97b,#38f9d7", // Green gradient
        "#fa709a,#fee140", // Orange gradient
        "#a8edea,#fed6e3"  // Mint gradient
    };

    public ChildCardAdapter(Context context, List<Child> children) {
        this.context = context;
        this.children = children;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_child_card, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Child child = children.get(position);
        
        // Set child information
        holder.childName.setText(child.getName());
        holder.childClass.setText(child.getClassName());
        holder.childGrade.setText(child.getGrade());
        holder.overallGrade.setText(child.getGradeDisplay());
        
        // Set performance status with color
        holder.performanceStatus.setText(child.getStatus().toUpperCase());
        holder.performanceStatus.setTextColor(Color.parseColor(child.getStatusColor()));
        
        // Set branch info
        String branchShort = getBranchShortName(child.getBranch());
        holder.childBranch.setText(branchShort);
        
        // Set student code
        if (child.getStudentCode() != null) {
            holder.studentCode.setText("ID: " + child.getStudentCode());
        }
        
        // Dynamic sizing based on number of children
        adjustCardSize(holder.cardView, children.size());
        
        // Set gradient background (cycle through colors)
        String gradient = gradientColors[position % gradientColors.length];
        setGradientBackground(holder.cardView, gradient);
        
        // Click listener to open child details
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChildDetailActivity.class);
            intent.putExtra("child_id", child.getChildId());
            intent.putExtra("child_name", child.getName());
            intent.putExtra("child_class", child.getClassName());
            intent.putExtra("child_grade", child.getGrade());
            intent.putExtra("child_branch", child.getBranch());
            intent.putExtra("student_code", child.getStudentCode());
            intent.putExtra("overall_average", child.getOverallAverage());
            intent.putExtra("status", child.getStatus());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    private void adjustCardSize(CardView cardView, int childrenCount) {
        ViewGroup.LayoutParams params = cardView.getLayoutParams();
        
        // Base height and adjust based on number of children
        int baseHeight = dpToPx(180); // Base height for 1 child
        
        if (childrenCount == 1) {
            params.height = baseHeight;
        } else if (childrenCount == 2) {
            params.height = (int) (baseHeight * 0.85); // 85% for 2 children
        } else if (childrenCount == 3) {
            params.height = (int) (baseHeight * 0.7); // 70% for 3 children
        } else {
            params.height = (int) (baseHeight * 0.6); // 60% for 4+ children
        }
        
        cardView.setLayoutParams(params);
    }

    private void setGradientBackground(CardView cardView, String gradientColors) {
        // This would need a custom drawable or programmatic gradient
        // For now, we'll use the card's default styling with elevation
        cardView.setCardElevation(dpToPx(8));
        cardView.setRadius(dpToPx(16));
    }

    private String getBranchShortName(String fullBranchName) {
        if (fullBranchName == null) return "Unknown";
        
        if (fullBranchName.contains("Kacyiru")) {
            return "Kacyiru Campus";
        } else if (fullBranchName.contains("Gisozi")) {
            return "Gisozi Campus";
        } else if (fullBranchName.contains("Kimisagara")) {
            return "Kimisagara Campus";
        }
        return "Eden Campus";
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public void updateChildren(List<Child> newChildren) {
        this.children = newChildren;
        notifyDataSetChanged();
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView childAvatar;
        TextView childName, childClass, childGrade, childBranch;
        TextView overallGrade, performanceStatus, studentCode;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.childCard);
            childAvatar = itemView.findViewById(R.id.childAvatar);
            childName = itemView.findViewById(R.id.childName);
            childClass = itemView.findViewById(R.id.childClass);
            childGrade = itemView.findViewById(R.id.childGrade);
            childBranch = itemView.findViewById(R.id.childBranch);
            overallGrade = itemView.findViewById(R.id.overallGrade);
            performanceStatus = itemView.findViewById(R.id.performanceStatus);
            studentCode = itemView.findViewById(R.id.studentCode);
        }
    }
}
