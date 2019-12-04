package com.example.myapplication


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import kotlin.Array

/**
 * DB를 관리하고 DB 정보에 접근하도록 하는 매개 클래스입니다.
 */
class StatManager {
    companion object {
        private lateinit var myContext: Context
        private const val DBVERSION = 4
        private lateinit var statDB : StatDB

        private val gson: Gson =  Gson()
        private const val fileName: String = "stats.json"
        private val dateDecoder = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")!!

        /**
         * 데이터베이스 시작합니다.
         */
        fun init(context: Context) {
            myContext = context
            statDB = StatDB(context)
        }

        /**
         * 프리셋을 데이터베이스 더합니다.
         */
        fun addPreset(name: String, givenTime: Int): Boolean {
            val dateTime: DateTime = DateTime.now()
            return statDB.insertData(name, givenTime, 0,0,0,0,gson.toJson(listOf<String>()), dateDecoder.print(dateTime))
        }

        /**
         * 프리셋 이름에 해당하는 프리셋 시간을 반환합니다.
         *
         * @param[name] 프리셋 이름
         * @return 프리셋 시간
         */
        fun getGivenTime(name: String): Int? {
            val cur = statDB.getByName(name)
            if (cur.count == 0 ) {
                throw Exception("No such name has been found")
            } else {
                cur.moveToFirst()
                return cur.getInt(cur.getColumnIndex(StatDB.COL_3))
                //return statDBAlt.getStatByID(name)?.givenTime
            }
        }

        /**
         * 프리셋 이름 리스트를 반환합니다.
         */
        fun getNames(): List<String> {
            val list = mutableListOf<String>()
            try {
                val res = statDB.getAllData()
                if (res.count != 0 ) {
                    while (res.moveToNext()) {
                        list.add(res.getString(res.getColumnIndex(StatDB.COL_2)))
                    }
                }
            } catch (e:Exception) {
                Log.d("ERROR", "$e : occured on getNames")
            } finally {
                return list
            }
        }

        /**
         * 프리셋 정보/통계를 업데이트 합니다.
         *
         * @param[result] 업데이트할 타이머 결과값입니다.
         */
        fun update(result: Result) {
            val success: Boolean
            val cur = statDB.getByName(result.name)
            cur.moveToFirst()

            val newHistory: MutableList<Result> = gson.fromJson(cur.getString(cur.getColumnIndex(StatDB.COL_8)), Array<Result>::class.java).toMutableList()
            newHistory.add(result)

            val dateTime: DateTime = DateTime.now()
            success = if (result.isOverTime) {
                statDB.updateData(result.name,0, 0, 1, result.spentTime - result.givenTime, gson.toJson(newHistory), dateDecoder.print(dateTime))
            } else {
                statDB.updateData(result.name,1, result.spentTime, 0, 0, gson.toJson(newHistory), dateDecoder.print(dateTime))
            }

            if (!success) {
                Toast.makeText(myContext, "업데이트 실패", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(myContext, "저장 완료", Toast.LENGTH_SHORT).show()
            }
        }

        /**
         * 프리셋 정보를 삭제합니다.
         *
         * @param[presetName] 삭제할 프리셋 이름
         */
        fun delete(presetName: String) {
            if (!statDB.deleteData(presetName)) {
                Toast.makeText(myContext, "삭제 실패", Toast.LENGTH_SHORT).show()
            }
        }

        /**
         * DB의 모든 데이터를 가져옵니다.
         */
        fun getStatistics() =
            statDB.getAllData()

        /**
         * DB의 모든 데이터를 Stat 클래스 리스트의 형태로 반환합니다.
         */
        fun getStatList(): MutableList<Stat> {
            val list: MutableList<Stat> = mutableListOf()
            val cursor = statDB.getAllData()
            var history: Array<Result>
            return if (cursor.count == 0) {
                Toast.makeText(myContext, "빈 리스트", Toast.LENGTH_SHORT).show()
                list
            } else {
                while(cursor.moveToNext()) {
                    history = gson.fromJson(cursor.getString(7), Array<Result>::class.java)
                    list.add(Stat(cursor.getString(1),
                            cursor.getInt(2),
                            cursor.getInt(3),
                            cursor.getInt(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            history.toMutableList() ))
                }
                list
            }
        }

        class StatDB(context: Context?) : SQLiteOpenHelper(context, DBNAME, null, DBVERSION) {
            companion object {
                const val DBNAME: String = "stats.db"
                const val TABLENAME: String = "stats_table"
                const val COL_1: String = "ID"
                const val COL_2: String = "NAME"
                const val COL_3: String = "GIVENTIME"
                const val COL_4: String = "NORMALCOUNT"
                const val COL_5: String = "NORMALTIMESUM"
                const val COL_6: String = "OVERCOUNT"
                const val COL_7: String = "OVERTIMESUME"
                const val COL_8: String = "HISTORY"
                const val COL_9: String = "LASTMODIFIED"
                // class Stat(val id: String, var givenTime: Int, var normalCount: Int ,var normalTimeSum: Int, var overCount: Int, var overTimeSum: Int, var history : MutableList<Result>)
            }

            /**
             * DB 테이블을 만드는 쿼리를 실행합니다.
             */
            override fun onCreate(db: SQLiteDatabase?) {
                db?.execSQL("create table $TABLENAME ($COL_1 INTEGER PRIMARY KEY AUTOINCREMENT, $COL_2 TEXT UNIQUE, $COL_3 INTEGER, $COL_4 INTEGER,$COL_5 INTEGER,$COL_6 INTEGER,$COL_7 INTEGER, $COL_8 TEXT, $COL_9 TEXT)")
            }

            /**
             * 버전이 바뀔 경우 DB를 업그레이드 합니다.
             */
            override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
                Log.d("LOG", "UPGRADED")
                db?.execSQL("DROP TABLE IF EXISTS $TABLENAME")
                onCreate(db)
            }

            /**
             * DB에 데이터를 추가하는 쿼리문을 실행합니다.
             */
            fun insertData(name: String, givenTime: Int, normalCount: Int, normalTimeSum: Int, overCount: Int, overTimeSum: Int, history: String, lastModified: String) : Boolean {
                val db :SQLiteDatabase = this.writableDatabase
                val contentValues = ContentValues()
                contentValues.put(COL_2, name)
                contentValues.put(COL_3, givenTime)
                contentValues.put(COL_4, normalCount)
                contentValues.put(COL_5, normalTimeSum)
                contentValues.put(COL_6, overCount)
                contentValues.put(COL_7, overTimeSum)
                contentValues.put(COL_8, history)
                contentValues.put(COL_9, lastModified)
                val result : Long = db.insert(TABLENAME, null, contentValues)
                return result.toInt() != -1
            }

            /**
             * DB에 담긴 데이터를 업데이트하는 쿼리문을 실행합니다.
             */
            fun updateData(name: String, normalCount: Int, normalTimeSum: Int, overCount: Int, overTimeSum: Int, history: String, lastModified: String): Boolean {
                val db :SQLiteDatabase = this.writableDatabase
                try {
                    db.execSQL("UPDATE $TABLENAME SET $COL_4 = $COL_4 + $normalCount, $COL_5 = $COL_5 + $normalTimeSum, $COL_6 = $COL_6 + $overCount, $COL_7 = $COL_7 + $overTimeSum, $COL_8 = '$history', $COL_9 = '$lastModified' WHERE NAME LIKE '%$name%'")
                } catch (e: Exception) {
                    Log.d("ERROR", "Failed to execute sql query. Most likely becuase of name")
                    Log.d("ERROR", "Full log is -> $e")
                    return false
                }
                return true
            }

            /**
             * DB에 담긴 데이터를 삭제하는 쿼리문을 실행합니다.
             */
            fun deleteData(name: String) : Boolean {
                val db :SQLiteDatabase = this.writableDatabase
                try {
                    db.execSQL("DELETE FROM $TABLENAME WHERE NAME LIKE '%$name%'")
                } catch (e: Exception) {
                    Log.d("ERROR", "Failed to execute sql query. Most likely becuase of name")
                    Log.d("ERROR", "Full log is -> $e")
                    return false
                }
                return true
            }
            /**
             * DB에 데이터를 이름으로 검색하여 Cursor로 반환하는 쿼리문을 실행합니다.
             */
            fun getByName(name: String): Cursor {
                val db : SQLiteDatabase = this.readableDatabase
                return db.rawQuery("SELECT * FROM $TABLENAME WHERE NAME LIKE '%$name%'", null)
            }
            /**
             * DB에 모든 데이터를 최신순으로 정렬하여 Cursor로 반환하는 쿼리문을 실행합니다.
             */
            fun getAllData(): Cursor {
                val db :SQLiteDatabase = this.readableDatabase
                return db.rawQuery("select * from $TABLENAME ORDER BY $COL_9 DESC", null)
            }
        }
    }
}

/**
 * 프리셋 기록을 DB에서 읽어와 메모리에 저장하는 클래스입니다.
 */
class Stat(val name: String, var givenTime: Int, var normalCount: Int, var normalTimeSum: Int, var overCount: Int, var overTimeSum: Int, var history : MutableList<Result>) {

    // 타이머 실행 총회수
    var totalCount : Int = 0
        get() = normalCount + overCount
        private set

    // 타이머 평균 회수
    var normalAverage: Float = 0f
        get() {
            if (normalCount <=0) {
                return 0f
            }
            return normalTimeSum.toFloat() / normalCount
        }
        private  set
    // 초과 타이머 평균 회수
    var overAverage: Float = 0f
        get() {
            if (overCount <=0) {
                return 0f
            }
            return overTimeSum.toFloat() / overCount
        }
        private  set

    /**
     * 타이머 기록을 주어진 길이만큼 반환합니다.
     * 타이머 기록은 최신순으로 반환됩니다.
     *
     * @param[count] 원하는 기록의 숫자입니다.
     */
    fun getHistory(count: Int): List<Result> {
        return when {
            history.size == 0 -> history
            history.size >= count -> history.reversed().subList(0, count)
            history.size < count -> history.reversed().subList(0, history.size)
            else -> history
        }
    }
}


/**
 * 타이머 종료시에 DB에 업데이트될 결과값 데이터 클래스입니다.
 */
data class Result(val name: String, val isOverTime: Boolean, var givenTime: Int, var spentTime: Int) {
    init {
        if (givenTime <= 0 ) {
            throw IllegalArgumentException("givenTime should be positive integer")
        }
        if (isOverTime && spentTime < givenTime) {
            throw IllegalArgumentException("spentTime should be bigger than givenTime if isOverTime is true")
        }
    }

    val statTime: Int
        get() {
            return if(!isOverTime) {
                spentTime
            } else {
                spentTime - givenTime
            }
        }
}