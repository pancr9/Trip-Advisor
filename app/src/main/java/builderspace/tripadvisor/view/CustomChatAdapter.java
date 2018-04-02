package builderspace.tripadvisor.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.HashMap;

import builderspace.tripadvisor.R;
import builderspace.tripadvisor.model.Message;
import builderspace.tripadvisor.model.User;

/**
 * Created by Rekhansh on 4/10/2017.
 */

public class CustomChatAdapter extends ArrayAdapter {

    private Context context;
    private ArrayList<Message> chatArrayList;
    private ChatOptions chatOptions;
    private HashMap<String, String> userKeyAndName;
    private User currentUser;

    private PrettyTime p;

    public CustomChatAdapter(Context context, int resource, ArrayList<Message> itemArrayList, ChatOptions co,
                             HashMap<String, String> userKeyAndName, User currentUser) {
        super(context, resource);
        this.context = context;
        int resource1 = resource;
        this.chatArrayList = itemArrayList;
        this.chatOptions = co;
        this.userKeyAndName = userKeyAndName;
        this.currentUser = currentUser;
        p = new PrettyTime();

    }

    @Override
    public int getCount() {
        return chatArrayList.size();
    }

    @Override
    public Message getItem(int position) {
        return chatArrayList.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Message chat = getItem(position);

        if (chat != null && chat.getImgUrl() != null && chat.getImgUrl().length() > 0) {
            if (chat.getName().equals(currentUser.getKey())) {
                convertView = inflater.inflate(R.layout.listview_chat_image_row_me, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.listview_chat_image_row, parent, false);
            }
            ImageView imageViewChatImage = convertView.findViewById(R.id.imageViewChatImage);
            ImageView imageViewDelete = convertView.findViewById(R.id.imageViewDelete);
            Picasso.with(context)
                    .load(chat.getImgUrl())
                    .into(imageViewChatImage);

            imageViewDelete.setOnClickListener(v -> callDelete(position));

        } else {
            if (chat.getName().equals(currentUser.getKey())) {
                convertView = inflater.inflate(R.layout.listview_chat_message_row_me, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.listview_chat_message_row, parent, false);
            }
            ImageView imageViewDelete = convertView.findViewById(R.id.imageViewDelete);
            TextView tv = convertView.findViewById(R.id.textViewChatMsg);
            tv.setText(chat.getText());

            imageViewDelete.setOnClickListener(v -> callDelete(position));
        }

        TextView name = convertView.findViewById(R.id.textViewSender);
        TextView time = convertView.findViewById(R.id.textViewTime);

        name.setText(userKeyAndName.get(chat.getName()));
        time.setText(p.format(chat.getTime()));

        return convertView;
    }

    private void callDelete(int position) {
        chatOptions.deleteChat(position);
    }

    public interface ChatOptions {
        void deleteChat(int position);
    }
}
