package mjtakenon.createmyjourney;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Const {

    /* IO関係 */
    static final String SAVEFILE =      "SavedJourney.csv";
    static final String COLUMN_INFO =   "info";
    static final String COLUMN_PLACE =  "place";

    /* Bundle受け渡し用変数 */
    static final String  MODE =         "MODE";
    static final Integer MODE_NEW =     0;
    static final Integer MODE_LOAD =    1;
    static final Integer MODE_ADD =     2;

    /* Bundle NEW用 */
    static final String  TIME_BEGIN =       "TIME_BEGIN";
    static final String  TIME_DURATION =    "TIME_DURATION";
    static final String  TIME_END =         "TIME_END";
    static final String  DATE_BEGIN =       "DATE_BEGIN";
//    static final String  DATE_END   =       "DATE_END";
    static final String  PLACE_BEGIN =      "PLACE_BEGIN";
    static final String  PLACE_END =        "PLACE_END";
    static final String  PLACE_DIST =       "PLACE_DIST";

    /* Bundle LOAD用 */
    static final String  PLACE_ID =          "ID";
    static final String  PLACE_NAME =       "NAME";
    static final String  JOURNEY_NAME =     "JOURNEY_NAME";
    static final String  TIME_ARRIVAL =     "TIME_ARRIVAL";
    static final String  TIME_DEPARTURE =   "TIME_DEPARTURE";

    static final String  JOURNEY =          "JOURNEY";

    /* Bundle New用 */
//    static final String  LATLNG_BEGIN =      "PLACE_BEGIN";
//    static final String  LATLNG_END =        "PLACE_BEGIN";
//    static final String  ADD_RESULT =        "RESULT";
//
//    static final Integer RESULT_OK =         0;
//    static final Integer RESULT_CANCEL =    1;
    static final String  SERIAL_PLACES =   "SERIAL_PLACES";

    /* Bundle Add用 */
    static final String  NEW_ID =           "NEW_ID";
    static final String  NEW_DIST =         "NEW_DIST";

    /* フォーマット形式 */
    static final DateFormat FORMAT_DATE = new SimpleDateFormat("yyyy/MM/dd");
    static final DateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm");

    /* リクエストID */
    static final Integer REQUEST_INSERT = 1;

    static final Integer RESULT_OK =         1;
    static final Integer RESULT_CANCEL =    0;

    /* APIキー */
    static final String KEY_FLICKR_API = "54943877e5144fdb63a83366c3549bc5";
    static final String KEY_GOOGLE_API = "AIzaSyDIoGExI7NPDFq6IJwVTDfyeDgca9q2OQQ";

    /* IDの割り当て */
    static final Integer ADDBUTTON_ID_BEGIN = 100;
    static final Integer TEXTMOVING_ID_BEGIN = 150;
    static final Integer TEXTPLACE_ID_BEGIN = 200;
}
