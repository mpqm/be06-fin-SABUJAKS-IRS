import { defineStore } from 'pinia';
import axios from 'axios';
import { backend } from '@/const';
import { toRaw } from 'vue';

export const UseAnnouncementStore = defineStore('announcement', {
    state: () => ({
        // 담당자 정보
        managerInfo: {
            managerName: '',
            phone1: '',
            phone2: '',
            phone3: '',
            managerEmail: '',
            isManagerNamePrivate: false,
            isPhonePrivate: true,
            isEmailPrivate: false
        },

        companyBenefits: [], // 복리후생 카테고리 및 세부 항목 리스트

        file: null,

        formData: {
            title: '',

            // 1. 모집분야
            recruitmentFieldName: '',
            numberOfRecruit: 0,
            isNewbie: false, // 신입 체크박스 상태
            isExperienced: false, // 경력 체크박스 상태
            mainDuty: '',
            department: '',
            position: '',
            requirement: '',

            // 2. 지원자격/근무조건
            isOverseas: false,
            isReworkPossible: false,
            workLocationCity: '',
            workLocationDetail: '',
            jobType: '',
            salaryType: '연봉',
            salaryAmount: 0,
            workHoursPerWeek: 0,
            isNegotiable: false,
            workDays: '주 5일(월~금)',
            startTime: '07시',
            endTime: '17시',

            // 3. 기업 복리후생
            additionalBenefits: '',

            // 4. 인사담당자/기업정보
            managerName: '',
            managerEmail: '',
            managerContact: '',
            companyIntro: '',

            // 5. 채용절차
            startDate: '', // 시작 날짜
            endDate: '',   // 마감 날짜
            startTimeRegi: '07시',
            endTimeRegi: '18시',
            announcementStart: '', // 백엔드로 넘길 시간
            announcementEnd: '', // 백엔드로 넘길 시간
            interviewCount: '선택해 주세요', // 면접 횟수
            recruitmentType: '마감일지정', // 모집 유형
            processSteps: '', // 전형절차

            // 6. 유의사항
            precautions: '',  // 유의사항 데이터
        },

    }),
    persist: {},
    actions: {
        // 채용담당자 정보 조회
        async fetchManagerInfo() {
            try {
                const response = await axios.get(
                    `${backend}/announcement/read-recruiter-info`,
                    {
                        headers: { 'Content-Type': 'application/json', },
                        withCredentials: true
                    });
                const data = response.data;
                console.log(data);

                // API 응답 데이터를 state에 저장
                this.managerInfo.managerName = data.result.managerName || '';
                this.managerInfo.phone1 = data.result.phone1 || '';
                this.managerInfo.phone2 = data.result.phone2 || '';
                this.managerInfo.phone3 = data.result.phone3 || '';
                this.managerInfo.managerEmail = data.result.managerEmail || '';
            } catch (error) {
                console.error('담당자 정보를 불러오지 못했습니다.', error);
            }
        },


        // 기업 복리후생 정보 조회
        async fetchCompanyBenefits(recruiterEmail) {
            try {
                const response = await axios.get(
                    `${backend}/announcement/read-company-info?recruiterEmail=${recruiterEmail}`,
                    {
                        headers: { 'Content-Type': 'application/json' },
                        // withCredentials: true
                    }
                );
                const data = response.data;
                console.log(data);

                // API 응답 데이터를 state에 저장
                this.companyBenefits = data.result.benefitsDataList || []; // result 자체가 리스트인데? 그래서 result를 넘김? x 
                // benefitsDataList에서 리스트 추출 = || []
                // console.log(this.companyBenefits);

            } catch (error) {
                console.error('기업 복리후생 정보를 불러오지 못했습니다.', error);
                throw new Error(error.response?.data?.message || '복리후생 정보 조회 실패');
            }
        },


        // formData를 백엔드로 전송하는 함수
        async createAnnouncement(selectedCategories, fields, fields2) {
            try {
                const formDataCreate = new FormData();


                let positionQuali = ""; // positionQuali를 빈 문자열로 초기화
                // fields 상태가 true일 때만 데이터를 추가
                if (fields.mainDuty) {
                    positionQuali += "주요 업무 : " + this.formData.mainDuty + " // ";
                }
                if (fields.department) {
                    positionQuali += "근무 부서 : " + this.formData.department + " // ";
                }
                if (fields.position) {
                    positionQuali += "직급 직책 : " + this.formData.position + " // ";
                }
                if (fields.requirement) {
                    positionQuali += "필수, 우대 조건 : " + this.formData.requirement;
                }

                // conditions 처리
                let conditions = "";

                // 근무 시간은 무조건 추가
                if (this.formData.workHoursPerWeek > 0) {
                    conditions += "근무 시간 : " + this.formData.workHoursPerWeek + "시간(주)";
                    if (this.formData.isNegotiable) {
                        conditions += " (면접 후 결정)";
                    }
                    conditions += " // ";
                }

                // fields2 상태를 확인하여 근무 요일 추가
                if (fields2.workDays && this.formData.workDays) {
                    conditions += "근무 요일 : " + this.formData.workDays + " // ";
                }

                // fields2 상태를 확인하여 출퇴근 시간 추가
                if (fields2.workTime && this.formData.startTime && this.formData.endTime) {
                    conditions += "출퇴근 시간 : " + this.formData.startTime + " ~ " + this.formData.endTime;
                }

                // 상태 객체를 toRaw로 비반응형으로 변환
                const rawFormData = toRaw(this.formData);
                const rawSelectedCategories = toRaw(selectedCategories.value);

                // 대분류와 소분류를 결합한 형태로 변환
                const jobCategoryList = Object.keys(rawSelectedCategories).reduce((acc, category) => {
                    const subcategories = rawSelectedCategories[category];
                    subcategories.forEach(subcategory => {
                        acc.push(`${category} > ${subcategory}`);
                    });
                    return acc;
                }, []);

                // 리스트를 쉼표로 구분된 문자열로 변환
                const jobCategoryString = jobCategoryList.join(", ");

                // 전송할 AnnouncementCreateReq 객체 생성
                const AnnouncementCreateReq = {
                    title: rawFormData.title, // 공고제목
                    selectForm: 0, // 양식선택

                    jobCategoryList: jobCategoryString, // 직무 카테고리에서 .value로 접근하여 일반 데이터로 변환
                    jobTitle: rawFormData.recruitmentFieldName, // 모집분야명
                    recruitedNum: rawFormData.numberOfRecruit, // 모집인원
                    careerBase: rawFormData.isNewbie ? "신입" : rawFormData.isExperienced ? "경력" : "", // 경력
                    positionQuali: positionQuali, // 조건에 따라 추가된 포지션&자격요건

                    region: rawFormData.workLocationCity + " " + rawFormData.workLocationDetail, // 근무지역
                    jobType: rawFormData.jobType, // 근무형태
                    salary: rawFormData.salaryType + " : " + rawFormData.salaryAmount + "만원", // 급여
                    conditions: conditions, // 조건에 따라 추가된 근무조건

                    benefits: rawFormData.additionalBenefits, // 복지&혜택

                    managerName: rawFormData.managerName, // 인사담당자명
                    managerContact: rawFormData.managerContact, // 인사담당자 연락처
                    managerEmail: rawFormData.managerEmail, // 인사담당자 이메일
                    intro: rawFormData.companyIntro, // 회사소개

                    announcementStart: rawFormData.announcementStart, // 모집시작
                    announcementEnd: rawFormData.announcementEnd, // 모집마감
                    interviewNum: rawFormData.interviewCount, // 면접횟수
                    process: rawFormData.processSteps, // 전형절차

                    note: rawFormData.precautions, // 유의사항

                    // recruitmentType: this.formData.recruitmentType,
                };

                // 조건에 따라 근무지역, 근무시간, 파일 유무 값 추가
                if (rawFormData.isOverseas) {
                    AnnouncementCreateReq.region += " (해외지역 출장)";
                }
                if (rawFormData.isReworkPossible) {
                    AnnouncementCreateReq.region += " (재택근무 가능)";
                }
                if (this.file && this.file instanceof File) {
                    AnnouncementCreateReq.selectForm = 1; // 파일이 있으면 1 넘기기
                } else {
                    AnnouncementCreateReq.selectForm = 0; // 파일이 없으면 0으로 설정
                }

                console.log("Raw formData before sending:", rawFormData);
                console.log("Serialized AnnouncementCreateReq:", AnnouncementCreateReq);

                // JSON 직렬화에서 순환 참조가 제거된 데이터를 전송
                const jsonBlob = new Blob([JSON.stringify(AnnouncementCreateReq)], { type: 'application/json' });
                formDataCreate.append('dto', jsonBlob);

                // 복사한 데이터 객체에서 순환 참조가 없는 데이터만 JSON으로 변환
                // const cleanedData = JSON.parse(JSON.stringify(AnnouncementCreateReq));
                // const jsonBlob = new Blob([JSON.stringify(cleanedData)], { type: 'application/json' });
                // formDataCreate.append('dto', jsonBlob);

                // 공고 이미지 있으면 url 추가
                if (this.file) {
                    formDataCreate.append('file', this.file);
                }


                // 백엔드로 POST 요청 보내기
                const response = await axios.post(`${backend}/announcement/create-step1`, formDataCreate, {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                        withCredentials: true
                    },
                });

                if (response.status === 200) {
                    alert('데이터가 성공적으로 저장되었습니다.');
                }
            } catch (error) {
                console.error('데이터 저장 실패:', error);
                alert('데이터 저장 중 오류가 발생했습니다.');
            }
        },

    },

});