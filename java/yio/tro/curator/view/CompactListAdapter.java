package yio.tro.curator.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import yio.tro.curator.R;
import yio.tro.curator.model.Rule;

import java.util.ArrayList;

public class CompactListAdapter extends ArrayAdapter<Rule>{


    public CompactListAdapter(Context context, ArrayList<Rule> rules) {
        super(context, R.layout.compact_list_row, rules);
    }


    static class ViewHolder {
        TextView textView;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.compact_list_row, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.compact_rule_text_view);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Rule rule = getItem(position);
        viewHolder.textView.setText(rule.getTitle());

        return convertView;
    }


    @Override
    public boolean isEnabled(int position) {
        return true;
    }

}
