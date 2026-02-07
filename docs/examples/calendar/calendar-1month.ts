// Calendar1Month.ts
import { CalendarBase } from "./calendar-base";
import { addYmd, zeroPad } from "./calendar-util";
import type { CalendarResponse } from "$lib/types";

export class Calendar1Month extends CalendarBase {
  constructor(
    year: number, month: number,
    data: CalendarResponse[]
  ) {
    super(year,month, data);
  }

  private choiceDayNumberClass(dayIndex: number, ymd: string): string {
    const thisYm = `${this.currentYear}${zeroPad(this.currentMonth)}`;
    const isHoliday = this.holidays.some((holiday) => holiday.ymd === ymd);
    const isThisMonth = ymd.startsWith(thisYm);
    const isSunday = dayIndex % 7 === 0;
    const isSaturday = dayIndex % 7 === 6;

    let css: string = "";
    if (!isThisMonth) {
      if (isHoliday || isSunday) {
        css = "not-this-month-holiday";
      }else if (isSaturday) {  
        css = "not-this-month-saterday";
      }else {
        css =  "not-this-month";
      }
      return css;
    }

    if (isHoliday || isSunday) {
      css =  "text-holiday";
    }else if (isSaturday) {
      css= "text-saterday";
    }

    if (this.today() == ymd) {
      return css + " text-today";
    }
    if( css != "") return css;
    return "text-normal";
  }

  private getEventClassName(gubun: string): string {
    if (gubun === 'E') return "gubun-event";
    else if (gubun === 'Y') return "gubun-year";
    else if (gubun === 'M') return "gubun-month";
    return "";
  }

  public renderCalendar(year: number, month: number): string {
    this.currentYear = year;
    this.currentMonth = month;

    const [startYmd, endYmd] = this.startEndYmd(year, month);
    const maxEventCount = this.getMaxEventCount(startYmd, endYmd);
    const heightStyle = maxEventCount >= 6 ? 'style="min-height: 200px;"' : "";

    let html = '<div class="row">';
    html += '<div class="col-week bg-light text-center text-danger week">일</div>';
    html += '<div class="col-week bg-light text-center week">월</div>';
    html += '<div class="col-week bg-light text-center week">화</div>';
    html += '<div class="col-week bg-light text-center week">수</div>';
    html += '<div class="col-week bg-light text-center week">목</div>';
    html += '<div class="col-week bg-light text-center week">금</div>';
    html += '<div class="col-week bg-light text-center text-saterday week">토</div>';
    html += "</div>";

    let i = 0;
    let saveCloseDiv = "";
    let ymd = startYmd;
    const today = this.today();

    while (ymd <= endYmd) {
      if (i % 7 === 0) {
        html += saveCloseDiv;
        html += '<div class="row">';
        saveCloseDiv = "</div>";
      }

      const isToday = ymd === today;
      let dayHtml = `<div class="col day${isToday ? " bg-today" : ""}" data-ymd="${ymd}" ${heightStyle}>`;
      const clsName = this.choiceDayNumberClass(i, ymd);
      dayHtml += `<span class="${clsName}">${parseInt(ymd.substring(6))}</span>`; //일자

      const holidayEvents = this.holidays.filter(
        (holiday) => holiday.ymd === ymd
      );
      holidayEvents.forEach((holiday) => {
        dayHtml += `<div class="holiday">${holiday.content}</div>`;
      });

      const eventDays = this.eventDays.filter((event) => event.ymd === ymd);
      eventDays.forEach((event) => {
        const eventClsName = this.getEventClassName(event.gubun);
        dayHtml += `<div class="${eventClsName} ipo_event">${event.content}</div>`;
      });

      dayHtml += "</div>";
      html += dayHtml;

      ymd = addYmd(ymd, 1);
      i++;
    }
    html += "</div>";
    return html;
  }
}
