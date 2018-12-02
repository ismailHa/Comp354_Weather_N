package ca.concordia.comp354mn.project.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import ca.concordia.comp354mn.project.R;

import java.util.ArrayList;
import java.util.List;

public class TwoLineArrayAdapter extends ArrayAdapter<ListItem> {

    private Context m_context;
    private List<ListItem> m_items;

    public TwoLineArrayAdapter(@NonNull Context context, List<ListItem> items) {
        super(context, 0 , items);
        m_context = context;
        m_items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(m_context).inflate(R.layout.twoline_list_item,null,false);

        ListItem currentItem = m_items.get(position);

        TextView header = (TextView) listItem.findViewById(R.id.listItemHeader);
        header.setText(currentItem.getHeader());

        TextView body = (TextView) listItem.findViewById(R.id.listItemBody);
        body.setText(currentItem.getBody());

        return listItem;
    }
}
