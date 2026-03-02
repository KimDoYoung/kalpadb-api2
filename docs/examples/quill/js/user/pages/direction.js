/**
 * Direction Page Initialization
 * src/main/resources/templates/user1/company/direction.html용 JavaScript
 */

import { initAlpineBase } from '../modules/alpine-base.js';
import { setupScrollListener } from '../modules/scroll.js';
import { setupThemeLangWatchers } from '../modules/theme-lang-watcher.js';

/**
 * Direction Page Alpine Data
 */
export function initDirectionPage() {
    return {
        ...initAlpineBase(),

        // Company submenu state
        submenuOpen: false,

        // Data
        companyData: {
            address: {
                label: { ko: '주소', en: 'Address' },
                value: {
                    ko: '서울시 영등포구 의사당대로 143 금융투자협회빌딩 5층',
                    en: '5th Floor, Korea Financial Investment Association Building, 143, Uisadang-daero, Yeongdeungpo-gu, Seoul'
                },
                geo: { lat: 37.519019862241, lng: 126.927568082638 },
                navigation_keywords: {
                    ko: ['의사당대로 143', '금융투자협회빌딩', '샛강역'],
                    en: ['143 Uisadang-daero', 'KOFIA Building', 'Saetgang Station']
                }
            },
            phone: {
                label: { ko: '전화', en: 'Tel' },
                value: { ko: '02-785-8100', en: '02-785-8100' }
            },
            fax: {
                label: { ko: '팩스', en: 'Fax' },
                value: { ko: '02-785-8101', en: '02-785-8101' }
            },
            email: {
                label: { ko: '이메일', en: 'Email' },
                value: { ko: 'info@k-fs.co.kr', en: 'info@k-fs.co.kr' }
            },
            subway: {
                label: { ko: '지하철', en: 'Subway' },
                list: [
                    {
                        ko: '5호선 여의도역 5번 출구 도보 5분',
                        en: 'Line 5 Yeouido Station Exit 5, 5 minutes walk'
                    },
                    {
                        ko: '9호선 샛강역 1번 출구 도보 2분',
                        en: 'Line 9 Saetgang Station Exit 1, 2 minutes walk'
                    }
                ]
            },
            bus: {
                label: { ko: '버스', en: 'Bus' },
                list: [
                    {
                        ko: '여의도역 정류장(여의도역 5번 출구 앞): 162, 360, 503, 262',
                        en: 'Yeouido Station Stop (Exit 5): Bus 162, 360, 503, 262'
                    }
                ]
            },
            parking: {
                label: { ko: '주차', en: 'Parking' },
                value: {
                    ko: '금융투자협회 지하주차장 이용 가능',
                    en: 'Available at KOFIA underground parking'
                }
            }
        },

        /**
         * x-init에서 호출될 초기화 함수
         */
        init() {
            // 1. Scroll 리스너 설정
            setupScrollListener(this);

            // 2. Dark mode와 lang 변경 감시
            setupThemeLangWatchers(this);

            // 3. Naver Map 초기화
            setTimeout(() => {
                this.initNaverMap();
            }, 100);

            // 4. lang 변경 감시
            this.$watch('lang', (newLang) => {
                this.changeMapLanguage(newLang);
            });
        },

        // 전화 걸기
        callPhone() {
            const phoneNumber = this.companyData.phone.value.ko.replace(/-/g, '');
            window.location.href = `tel:${phoneNumber}`;
        },

        // 이메일 열기
        sendEmail() {
            const email = this.companyData.email.value.en;
            window.location.href = `mailto:${email}`;
        },

        // 지도 열기 (Google Maps - 좌표 기반)
        openMapGoogle() {
            const { lat, lng } = this.companyData.address.geo;
            const url = `https://www.google.com/maps/@${lat},${lng},15z`;
            window.open(url, '_blank');
        },

        // 네이버 지도 열기
        openMapNaver() {
            const { lat, lng } = this.companyData.address.geo;
            // const url = `https://map.naver.com/index.nhn?c=${lng},${lat},15,0,0,0,dh`;
            //https://map.naver.com/p/search/korea%20fund%20service?c=15.00,0,0,0,dh
            const url = `https://map.naver.com/p/search/korea%20fund%20service?c=15.00,0,0,0,dh`;
            window.open(url, '_blank');
        },

        // Naver Map 초기화
        initNaverMap() {
            if (!window.naver || !window.naver.maps) {
                console.error('Naver Maps API not loaded');
                return;
            }
            
            const { lat, lng } = this.companyData.address.geo;
            const mapContainer = document.getElementById('naverMap');
            
            if (!mapContainer) {
                console.error('Map container not found');
                return;
            }

            const mapOptions = {
                center: new naver.maps.LatLng(lat, lng),
                zoom: 15,
                minZoom: 7,
                zoomControl: true,
                zoomControlOptions: {
                    position: naver.maps.Position.TOP_RIGHT
                }
            };

            this.naverMapInstance = new naver.maps.Map(mapContainer, mapOptions);

            // 마커 추가
            const marker = new naver.maps.Marker({
                position: new naver.maps.LatLng(lat, lng),
                map: this.naverMapInstance,
                icon: {
                    url: '/assets/images/pin2.svg',
                    size: new naver.maps.Size(22, 35),
                    origin: new naver.maps.Point(0, 0),
                    anchor: new naver.maps.Point(11, 35)
                }
            });

            // Infowindow 추가
            const infoContent = this.lang === 'ko' ? '한국펀드서비스' : 'Korea Fund Service';
            const infowindow = new naver.maps.InfoWindow({
                content: `<div class="p-2 text-center text-sm font-semibold">${infoContent}</div>`,
                position: new naver.maps.LatLng(lat, lng)
            });
            infowindow.open(this.naverMapInstance);
        },

        // 언어 변경 시 Naver Map 언어 변경
        changeMapLanguage(lang) {
            if (this.naverMapInstance) {
                this.naverMapInstance = null;
            }

            // 기존 스크립트 제거
            const oldScript = document.getElementById('naverMapScript');
            if (oldScript) {
                oldScript.remove();
            }

            // Naver Maps API 전역 객체 제거
            if (window.naver) {
                delete window.naver;
            }

            // 새 언어로 스크립트 추가
            const newScript = document.createElement('script');
            newScript.type = 'text/javascript';
            newScript.id = 'naverMapScript';
            newScript.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=4ocxbc3blq&language=${lang}&t=${Date.now()}`;
            newScript.onload = () => {
                setTimeout(() => {
                    this.initNaverMap();
                }, 100);
            };
            document.head.appendChild(newScript);
        }
    };
}
