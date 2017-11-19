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
    static final Integer MODE_ADD =     0;
    static final Integer MODE_LOAD =    1;

    /* Bundle ADD用 */
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

    /* Format */
    static final DateFormat FORMAT_DATE = new SimpleDateFormat("yyyy/MM/dd");
    static final DateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm");

    /* APIキー */
    static final String KEY_FLICKR_API = "54943877e5144fdb63a83366c3549bc5";
    static final String KEY_GOOGLE_API = "AIzaSyDIoGExI7NPDFq6IJwVTDfyeDgca9q2OQQ";


    static final Integer ADD_BUTTON_ID_BEGIN = 50;
}
