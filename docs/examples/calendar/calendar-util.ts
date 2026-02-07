// calendar-util.ts

/** 숫자가 10 미만일 경우 앞에 0을 붙여 자리수를 맞춰주는 함수 */
export function zeroPad(value: number): string {
  return value < 10 ? "0" + value : value.toString();
}

/** 특정 날짜의 요일을 반환 (일요일=0, 월요일=1, ...) */
export function yoilNumber(year: number, month: number, day: number): number {
  const date = new Date(year, month - 1, day);
  return date.getDay();
}

/** 날짜에서 일정 일 수를 뺀 날짜(yyyymmdd 형식)를 반환 */
export function substractYmd(ymd: string, days: number): string {
  const date = new Date(
    parseInt(ymd.substring(0, 4)),
    parseInt(ymd.substring(4, 6)) - 1,
    parseInt(ymd.substring(6))
  );
  date.setDate(date.getDate() - days);
  return `${date.getFullYear()}${zeroPad(date.getMonth() + 1)}${zeroPad(date.getDate())}`;
}

/** 날짜에 일정 일 수를 더한 날짜(yyyymmdd 형식)를 반환 */
export function addYmd(ymd: string, days: number): string {
  const date = new Date(
    parseInt(ymd.substring(0, 4)),
    parseInt(ymd.substring(4, 6)) - 1,
    parseInt(ymd.substring(6))
  );
  date.setDate(date.getDate() + days);
  return `${date.getFullYear()}${zeroPad(date.getMonth() + 1)}${zeroPad(date.getDate())}`;
}

/** 해당 월의 마지막 날짜를 yyyymmdd 형식으로 반환 */
export function getEndYmd(year: number, month: number): string {
  const lastDay = new Date(year, month, 0).getDate();
  return `${year}${zeroPad(month)}${zeroPad(lastDay)}`;
}

/** 오늘 날짜를 yyyymmdd 형식으로 반환 */
export function todayYmd(): string {
  const date = new Date();
  return `${date.getFullYear()}${zeroPad(date.getMonth() + 1)}${zeroPad(date.getDate())}`;
}

/** 문자열을 특정 길이로 자르고 '...' 추가 */
export function cutString(str: string, len: number): string {
  return str.length > len ? str.substring(0, len) + "..." : str;
}
