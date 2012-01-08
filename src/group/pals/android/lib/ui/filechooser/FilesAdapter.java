package group.pals.android.lib.ui.filechooser;

import group.pals.android.lib.ui.filechooser.utils.Converter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * The adapter to be used in {@link android.widget.ListView}
 * @author Haiti Meid
 *
 */
public class FilesAdapter extends ArrayAdapter<DataModel> {

  private final boolean MultiSelection;
  private final int SelectionMode;

  /**
   * Creates new {@link FilesAdapter}
   * @param context {@link Context}
   * @param textViewResourceId resource id to be used for this adapter
   * @param objects the data
   * @param selectionMode see {@link FileChooserActivity}
   * @param multiSelection see {@link FileChooserActivity}
   */
  public FilesAdapter(Context context, int textViewResourceId,
      List<DataModel> objects, int selectionMode, boolean multiSelection) {
    super(context, textViewResourceId, objects);
    this.SelectionMode = selectionMode;
    this.MultiSelection = multiSelection;
  }

  /**
   * The "view holder"
   * @author Haiti Meid
   *
   */
  private static final class Bag {
    TextView txtFileName;
    TextView txtFileInfo;
    CheckBox checkboxSelection;
    ImageView imageIcon;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final DataModel Data = getItem(position);
    Bag bag;

    if (convertView == null) {
      LayoutInflater layoutInflater = ((Activity) getContext()).getLayoutInflater();
      convertView = layoutInflater.inflate(R.layout.file_item, null);

      bag = new Bag();
      bag.txtFileName = (TextView) convertView.findViewById(R.id.text_view_filename);
      bag.txtFileInfo = (TextView) convertView.findViewById(R.id.text_view_file_info);
      bag.checkboxSelection = (CheckBox) convertView.findViewById(R.id.checkbox_selection);
      bag.imageIcon = (ImageView) convertView.findViewById(R.id.image_view_icon);

      convertView.setTag(bag);
    } else {
      bag = (Bag) convertView.getTag();
    }

    //image icon
    if (Data.getFile().isDirectory())
      bag.imageIcon.setImageResource(R.drawable.folder48);
    else
      bag.imageIcon.setImageResource(R.drawable.file48);

    //filename
    bag.txtFileName.setText(Data.getFile().getName());

    //file info
    if (Data.getFile().isDirectory())
      bag.txtFileInfo.setVisibility(View.GONE);
    else {
      bag.txtFileInfo.setText(Converter.sizeToStr(Data.getFile().length()));
      bag.txtFileInfo.setVisibility(View.VISIBLE);
    }

    //checkbox
    if (MultiSelection) {
      if (SelectionMode == FileChooserActivity.FilesOnly && Data.getFile().isDirectory()) {
        bag.checkboxSelection.setVisibility(View.GONE);
      } else {
        bag.checkboxSelection.setVisibility(View.VISIBLE);
        bag.checkboxSelection.setFocusable(false);
        bag.checkboxSelection.setOnCheckedChangeListener(
            new CompoundButton.OnCheckedChangeListener() {
  
              @Override
              public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Data.setSelected(isChecked);
              }
            });
        bag.checkboxSelection.setChecked(Data.isSelected());
      }
    } else
      bag.checkboxSelection.setVisibility(View.GONE);

    return convertView;
  }
}
