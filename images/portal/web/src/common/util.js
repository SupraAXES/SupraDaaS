function isChinese(str, index) {
  if (!str) {
    return false;
  }
  return str.charCodeAt(index) >= 10000;
}

export function trucateInBegin(str, length) {
    if (!str) {
      return '';
    }
    let sum = 0;
    let index = 0;
    for (; index < str.length && sum < length; index++) {
      if (isChinese(str, index)) {
        sum += 2;
      } else {
        sum += 1;
      }
    }
    return str.substring(0, index);
}
  
export function trucateInMiddle(str, prefix, suffix) {
    if (!str) {
      return '';
    }
    let length = [];
    let sum = 0;
    let preIndex = -1;
    let sufIndex = -1;
    let suffixHaveChinese = false;
    for (let i = 0; i < str.length; i++) {
      if (isChinese(str, i)) {
        sum += 2;
      } else {
        sum += 1;
      }
      length.push(sum);
      if (sum >= prefix && preIndex < 0) {
        preIndex = i;
      }
    }
    for (sufIndex = str.length - 1; sum - length[sufIndex] < suffix; sufIndex--) {
      if (isChinese(str, sufIndex)) {
        suffixHaveChinese = true;
      }
    }
    if (sum > prefix + suffix) {
      return str.substring(0, preIndex + 1) + '...' + str.substring(sufIndex + 2, str.length);
    } else {
      return str;
    }
}
