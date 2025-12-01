package com.example.neurotrack.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.neurotrack.models.TaskInstance;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TaskNotificationScheduler {

    private static final SimpleDateFormat API_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat API_TIME_FORMAT =
            new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public static void scheduleTaskNotifications(Context context, Long childId, List<TaskInstance> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        for (TaskInstance task : tasks) {
            scheduleSingleTask(context, childId, task);
        }
    }

    private static void scheduleSingleTask(Context context, Long childId, TaskInstance task) {
        if (task == null) return;

        String status = task.getStatus();

        if (!"PENDING".equals(status) && !"IN_PROGRESS".equals(status)) {
            return;
        }

        String dateStr = task.getScheduledFor();
        String timeStr = task.getPlannedTime();
        if (dateStr == null || timeStr == null || dateStr.isEmpty() || timeStr.isEmpty()) {
            return;
        }

        Date date;
        Date time;
        try {
            date = API_DATE_FORMAT.parse(dateStr);
            time = API_TIME_FORMAT.parse(timeStr);
        } catch (ParseException e) {
            return;
        }

        if (date == null || time == null) {
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(time);

        cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.add(Calendar.MINUTE, -10);

        long triggerAtMillis = cal.getTimeInMillis();
        long now = System.currentTimeMillis();
        if (triggerAtMillis <= now) {

            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra("TASK_ID", task.getTaskInstanceId() != null ? task.getTaskInstanceId() : -1L);
        intent.putExtra("CHILD_ID", childId != null ? childId : -1L);
        intent.putExtra("TASK_NAME", task.getTaskName());

        String displayTime = null;
        if (timeStr.length() >= 5) {
            displayTime = timeStr.substring(0, 5);
        }
        intent.putExtra("TASK_TIME", displayTime);

        int requestCode = (int) (task.getTaskInstanceId() != null ?
                (task.getTaskInstanceId() & 0x7FFFFFFF) :
                (System.currentTimeMillis() & 0x7FFFFFFF));

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                flags
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }
}

