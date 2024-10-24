import React from 'react';
import { Input } from 'antd';
import { ProFormText, ProFormTextArea } from '@ant-design/pro-form';

export const SupraFormText = (props) => (
  <ProFormText
    {...props}
    transform={(value) => ({
      [props.name]: value && typeof value === 'string' ? value.trim() : value,
    })}
  />
);

export const SupraFormTextPassword = (props) => (
  <ProFormText.Password
    {...props}
    transform={(value) => ({
      [props.name]: value && typeof value === 'string' ? value.trim() : value,
    })}
  />
);

export const SupraFormTextArea = (props) => (
  <ProFormTextArea
    {...props}
    transform={(value) => ({
      [props.name]: value && typeof value === 'string' ? value.trim() : value,
    })}
  />
);

export const createWrapperInput = (WrapperInput) => {
  return ({ handleChange, ...props }) => {
    const handleBlur = (e) => {
      const value = e.target.value.trim();
      if (typeof handleChange === 'function') {
        handleChange(value);
      }
    };

    return <WrapperInput {...props} onBlur={handleBlur} />;
  };
};

export const SupraInput = createWrapperInput(Input);

export const SupraInputPassword = createWrapperInput(Input.Password);

export const SupraInputTextArea = createWrapperInput(Input.TextArea);
