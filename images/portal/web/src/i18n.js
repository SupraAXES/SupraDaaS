import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import resources from './assets/locales/resources';

let localLang = window.localStorage.getItem('locale');
if (!localLang) {
    localLang = navigator.language === 'zh-CN' ? 'zh-CN' : 'en-US';
}

i18n
  .use(initReactI18next)
  .init({
    lng: localLang,
    resources,
    debug: true,
    interpolation: {
      escapeValue: false
    }
  });

export default i18n;
