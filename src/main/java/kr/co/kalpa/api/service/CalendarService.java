package kr.co.kalpa.api.service;

import com.github.usingsky.calendar.KoreanLunarCalendar;
import kr.co.kalpa.api.entity.Calendar;
import kr.co.kalpa.api.entity.CalendarPublic;
import kr.co.kalpa.api.repository.CalendarPublicRepository;
import kr.co.kalpa.api.repository.CalendarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {

    private final CalendarRepository calendarRepository;
    private final CalendarPublicRepository calendarPublicRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCalendarEvents(String startYmd, String endYmd) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 1. Get Public Events (Holidays, etc.)
        List<CalendarPublic> publicEvents = calendarPublicRepository.findByYmdBetween(startYmd, endYmd);
        for (CalendarPublic event : publicEvents) {
            addEvent(result, event.getYmd(), event.getContent(), event.getDataType(), true);
        }

        // 2. Get Solar Fixed Events from Calendar
        List<Calendar> solarFixedEvents = calendarRepository.findSolarFixedEventsInRange(startYmd, endYmd);
        for (Calendar event : solarFixedEvents) {
            String type = "EVENT";
            if ("H".equals(event.getGubun()))
                type = "HOLIDAY";
            addEvent(result, event.getYmd(), event.getContent(), type, false);
        }

        // 3. Process Repeating & Lunar Events
        List<Calendar> complexEvents = calendarRepository.findRepeatingOrLunarEvents();

        LocalDate start = LocalDate.parse(startYmd, DateTimeFormatter.BASIC_ISO_DATE);
        LocalDate end = LocalDate.parse(endYmd, DateTimeFormatter.BASIC_ISO_DATE);

        KoreanLunarCalendar lunarCalendar = KoreanLunarCalendar.getInstance();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String currentDateYmd = date.format(DateTimeFormatter.BASIC_ISO_DATE);
            String currentMmDd = currentDateYmd.substring(4);
            String currentDd = currentDateYmd.substring(6);

            // Calculate Lunar Date for current Solar Date
            lunarCalendar.setSolarDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
            String lunarYmd = lunarCalendar.getLunarIsoFormat().replace("-", ""); // YYYYMMDD
            String lunarMmDd = lunarYmd.substring(4);
            String lunarDd = lunarYmd.substring(6);
            // boolean isLeap = lunarCalendar.isIntercalation(); // Need to check how
            // library handles leap months in output or setting

            for (Calendar event : complexEvents) {
                boolean isMatch = false;

                if ("S".equals(event.getSorl())) {
                    // Solar Repeating
                    if ("Y".equals(event.getGubun())) { // Yearly
                        if (currentMmDd.equals(event.getYmd()))
                            isMatch = true;
                    } else if ("M".equals(event.getGubun())) { // Monthly
                        if (currentDd.equals(event.getYmd()))
                            isMatch = true;
                    }
                } else {
                    // Lunar
                    // Check logic based on gubun
                    if ("H".equals(event.getGubun()) || "E".equals(event.getGubun())) { // Fixed Lunar
                        if (lunarYmd.equals(event.getYmd()))
                            isMatch = true;
                    } else if ("Y".equals(event.getGubun())) { // Yearly Lunar
                        if (lunarMmDd.equals(event.getYmd()))
                            isMatch = true;
                    } else if ("M".equals(event.getGubun())) { // Monthly Lunar
                        if (lunarDd.equals(event.getYmd()))
                            isMatch = true;
                    }
                }

                if (isMatch) {
                    String type = "EVENT";
                    if ("H".equals(event.getGubun()))
                        type = "HOLIDAY";
                    addEvent(result, currentDateYmd, event.getContent(), type, false);
                }
            }
        }

        return result;
    }

    private void addEvent(List<Map<String, Object>> list, String ymd, String content, String type, boolean isPublic) {
        Map<String, Object> map = new HashMap<>();
        map.put("ymd", ymd);
        map.put("content", content);
        map.put("type", type);
        map.put("isPublic", isPublic);
        list.add(map);
    }
}
