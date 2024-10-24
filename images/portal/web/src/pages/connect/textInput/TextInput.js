import React, { useState } from 'react';
import { Input, Button } from 'antd';

import './TextInput.css';
import { SupraInputTextArea } from '../../../components/input/SupraInput';

let TextInput = (props) => {
  const { onButtonPressed, onTextInputFocus,ctrlKeyPressed, altKeyPressed, tabKeyPressed, shiftKeyPressed, textValue, onKeyDown, onKeyUp, onTextChange } = props;
  return (
    <div className='text-input'>
      <div className='text-input-field'>
        <SupraInputTextArea handleChange={onTextChange} onKeyDown={onKeyDown} onKeyUp={onKeyUp} value={textValue} rows={1} className='target' autoCorrect='off' autoCapitalize='off' onBlur={() => onTextInputFocus(false)} onFocus={() => onTextInputFocus(true)}/>
      </div>
      <div className='text-input-buttons'>
        <Button className={ctrlKeyPressed ? 'pressed-btn' : 'btn'} onClick={() => onButtonPressed('Ctrl')}>
          {'Ctrl'}
        </Button>
        <Button className={altKeyPressed ? 'pressed-btn' : 'btn'} onClick={() => onButtonPressed('Alt')}>
          {'Alt'}
        </Button>
        <Button className={shiftKeyPressed ? 'pressed-btn' : 'btn'} onClick={() => onButtonPressed('Shift')}>
          {'Shift'}
        </Button>
        <Button className={tabKeyPressed ? 'pressed-btn' : 'btn'} onClick={() => onButtonPressed('Tab')}>
          {'Tab'}
        </Button>
      </div>
    </div>
  );
};

export default TextInput;
