import { DatePicker, Space, Button } from 'antd';
import { useState } from 'react';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;

const DateRangePicker = ({ value, onChange, allowClear = true }) => {
  const [selectedRange, setSelectedRange] = useState(value || null);

  const quickOptions = [
    { label: '今天', getRange: () => [dayjs(), dayjs()] },
    { label: '昨天', getRange: () => [dayjs().subtract(1, 'day'), dayjs().subtract(1, 'day')] },
    { label: '本周', getRange: () => [dayjs().startOf('week'), dayjs().endOf('week')] },
    { label: '上周', getRange: () => [dayjs().subtract(1, 'week').startOf('week'), dayjs().subtract(1, 'week').endOf('week')] },
    { label: '本月', getRange: () => [dayjs().startOf('month'), dayjs().endOf('month')] },
    { label: '上月', getRange: () => [dayjs().subtract(1, 'month').startOf('month'), dayjs().subtract(1, 'month').endOf('month')] },
    { label: '近7天', getRange: () => [dayjs().subtract(6, 'day'), dayjs()] },
    { label: '近30天', getRange: () => [dayjs().subtract(29, 'day'), dayjs()] },
  ];

  const handleQuickSelect = (getRange) => {
    const range = getRange();
    setSelectedRange(range);
    onChange?.(range);
  };

  const handleRangeChange = (dates) => {
    setSelectedRange(dates);
    onChange?.(dates);
  };

  const handleClear = () => {
    setSelectedRange(null);
    onChange?.(null);
  };

  return (
    <Space.Compact style={{ width: '100%' }}>
      <Space wrap size={4} style={{ marginBottom: 8 }}>
        {quickOptions.map((option) => (
          <Button
            key={option.label}
            size="small"
            type={
              selectedRange &&
              selectedRange[0]?.isSame(option.getRange()[0], 'day') &&
              selectedRange[1]?.isSame(option.getRange()[1], 'day')
                ? 'primary'
                : 'default'
            }
            onClick={() => handleQuickSelect(option.getRange)}
          >
            {option.label}
          </Button>
        ))}
        {allowClear && selectedRange && (
          <Button size="small" onClick={handleClear}>
            清除
          </Button>
        )}
      </Space>
      <RangePicker
        style={{ width: '100%' }}
        value={selectedRange}
        onChange={handleRangeChange}
        allowClear={allowClear}
      />
    </Space.Compact>
  );
};

export default DateRangePicker;
