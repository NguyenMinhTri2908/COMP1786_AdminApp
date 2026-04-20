package com.example.coursework.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Khai báo 2 bảng dữ liệu và version. exportSchema = false để tránh cảnh báo.
@Database(entities = {ProjectEntity.class, ExpenseEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Kết nối với DAO
    public abstract AppDao appDao();

    // Biến lưu trữ phiên bản duy nhất của Database (Singleton pattern)
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // Tạo file database có tên là "coursework_database" trong máy
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "coursework_database")
                            // .fallbackToDestructiveMigration() giúp app không bị sập khi bạn đổi cấu trúc bảng sau này
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
