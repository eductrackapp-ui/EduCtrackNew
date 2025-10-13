package com.equipe7.eductrack.Activity;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.equipe7.eductrack.R;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity {

    public static List<ParentHomeActivity.SearchableItem> results;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        TextView tvTitle = findViewById(R.id.tvTitle);
        RecyclerView recyclerView = findViewById(R.id.recyclerResults);

        tvTitle.setText("Search Results");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SearchResultAdapter(results));
    }

    // Adapter pour afficher les résultats
    public static class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
        private final List<ParentHomeActivity.SearchableItem> items;

        public SearchResultAdapter(List<ParentHomeActivity.SearchableItem> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ParentHomeActivity.SearchableItem item = items.get(position);
            holder.text1.setText(item.name + " (" + item.code + ")");
            holder.text2.setText("Class: " + item.className);
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ViewHolder(android.view.View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}