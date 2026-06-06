import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Input, Dropdown, Avatar, Tag } from 'antd'
import { SearchOutlined, ProjectOutlined, TaskOutlined } from '@ant-design/icons'
import { quickSearch } from '@/services/search'
import styles from './GlobalSearch.module.scss'

const { Search } = Input

const GlobalSearch = () => {
  const [keyword, setKeyword] = useState('')
  const [searchResults, setSearchResults] = useState([])
  const [dropdownVisible, setDropdownVisible] = useState(false)
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    const timer = setTimeout(() => {
      if (keyword.trim()) {
        handleSearch(keyword)
      } else {
        setSearchResults([])
      }
    }, 300)
    return () => clearTimeout(timer)
  }, [keyword])

  const handleSearch = async (value) => {
    if (!value.trim()) return
    setLoading(true)
    try {
      const data = await quickSearch(value)
      setSearchResults(data || [])
      setDropdownVisible(true)
    } catch (error) {
      console.error('Search error:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSearchSubmit = (value) => {
    if (value.trim()) {
      navigate(`/search?keyword=${encodeURIComponent(value)}`)
      setDropdownVisible(false)
      setKeyword('')
    }
  }

  const handleResultClick = (item) => {
    if (item.type === 'project') {
      navigate(`/project/${item.id}`)
    } else if (item.type === 'task') {
      navigate(`/task/${item.id}`)
    }
    setDropdownVisible(false)
    setKeyword('')
  }

  const dropdownRender = () => {
    if (!keyword.trim()) return null

    if (loading) {
      return (
        <div className={styles.searchDropdown}>
          <div className={styles.searchLoading}>搜索中...</div>
        </div>
      )
    }

    if (searchResults.length === 0) {
      return (
        <div className={styles.searchDropdown}>
          <div className={styles.searchEmpty}>未找到相关结果</div>
          <div
            className={styles.searchAllBtn}
            onClick={() => handleSearchSubmit(keyword)}
          >
            搜索全部 "{keyword}"
          </div>
        </div>
      )
    }

    const projects = searchResults.filter((item) => item.type === 'project')
    const tasks = searchResults.filter((item) => item.type === 'task')

    return (
      <div className={styles.searchDropdown}>
        {projects.length > 0 && (
          <div className={styles.searchGroup}>
            <div className={styles.searchGroupTitle}>
              <ProjectOutlined /> 项目
            </div>
            {projects.slice(0, 5).map((item) => (
              <div
                key={`project-${item.id}`}
                className={styles.searchItem}
                onClick={() => handleResultClick(item)}
              >
                <Avatar size="small" icon={<ProjectOutlined />} />
                <span className={styles.searchItemTitle}>{item.name}</span>
                <Tag color="blue" className={styles.searchItemTag}>
                  项目
                </Tag>
              </div>
            ))}
          </div>
        )}

        {tasks.length > 0 && (
          <div className={styles.searchGroup}>
            <div className={styles.searchGroupTitle}>
              <UnorderedListOutlined /> 任务
            </div>
            {tasks.slice(0, 5).map((item) => (
              <div
                key={`task-${item.id}`}
                className={styles.searchItem}
                onClick={() => handleResultClick(item)}
              >
                <Avatar size="small" icon={<UnorderedListOutlined />} />
                <span className={styles.searchItemTitle}>{item.name}</span>
                <Tag color="orange" className={styles.searchItemTag}>
                  任务
                </Tag>
              </div>
            ))}
          </div>
        )}

        <div
          className={styles.searchAllBtn}
          onClick={() => handleSearchSubmit(keyword)}
        >
          查看全部搜索结果
        </div>
      </div>
    )
  }

  return (
    <Dropdown
      open={dropdownVisible}
      onOpenChange={setDropdownVisible}
      dropdownRender={dropdownRender}
      placement="bottomLeft"
      trigger={['click']}
    >
      <Search
        placeholder="搜索项目、任务..."
        allowClear
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
        onSearch={handleSearchSubmit}
        prefix={<SearchOutlined />}
        className={styles.searchInput}
      />
    </Dropdown>
  )
}

export default GlobalSearch
