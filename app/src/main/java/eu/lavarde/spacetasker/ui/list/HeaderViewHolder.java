package eu.lavarde.spacetasker.ui.list;

import android.database.Cursor;

import eu.lavarde.spacetasker.R;
import eu.lavarde.spacetasker.data.model.sql.Task;
import eu.lavarde.spacetasker.databinding.TasklistHeaderBinding;
import eu.lavarde.spacetasker.util.TimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

class HeaderViewHolder extends ViewHolder {

    private TaskListFragment taskListFragment;
    private final TasklistHeaderBinding binding;
    private final SimpleDateFormat weekdayFormatter;


    HeaderViewHolder(TaskListFragment taskListFragment, TasklistHeaderBinding binding) {
        super(binding.getRoot());
        this.taskListFragment = taskListFragment;
        this.binding = binding;
        weekdayFormatter = TimeFormatter.getLocalFormatterWeekday(taskListFragment.getContext());
    }

    @Override
    public void onBind(final Cursor cursor) {
        switch (cursor.getString(cursor.getColumnIndex(Task.Columns.TITLE))) {
            case Task.HEADER_KEY_OVERDUE:
                binding.text.setText(taskListFragment.getContext().getString(R.string.date_header_overdue));
                break;
            case Task.HEADER_KEY_TODAY:
                binding.text.setText(taskListFragment.getContext().getString(R.string.date_header_today));
                break;
            case Task.HEADER_KEY_PLUS1:
                binding.text.setText(taskListFragment.getContext().getString(R.string.date_header_tomorrow));
                break;
            case Task.HEADER_KEY_PLUS2:
            case Task.HEADER_KEY_PLUS4:
            case Task.HEADER_KEY_PLUS3:
                binding.text.setText(weekdayFormatter.format(new Date(cursor.getLong(4))));
                break;
            case Task.HEADER_KEY_LATER:
                binding.text.setText(taskListFragment.getContext().getString(R.string.date_header_future));
                break;
            case Task.HEADER_KEY_NODATE:
                binding.text.setText(taskListFragment.getContext().getString(R.string.date_header_none));
                break;
            case Task.HEADER_KEY_COMPLETE:
                binding.text.setText(taskListFragment.getContext().getString(R.string.date_header_completed));
                break;
        }
    }
}
