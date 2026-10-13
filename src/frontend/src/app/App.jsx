import {BrowserRouter, Navigate, NavLink, Route, Routes} from 'react-router-dom'
import Overview from "../pages/Overview.jsx";

export default function App() {
    const routes = [
        { path: '/', label: 'Overview', element: <Overview /> },
        { path: '/telemetry', label: 'Telemetry', element: <Overview /> },
        { path: '/commands', label: 'Commands', element: <Overview /> },
    ]
  return (
      <BrowserRouter>
          <div className="app">
              <div className="sidebar">
                  <div className="sidebar-header">
                      <h2 className="sidebar-title">CubeSat TTCS:</h2>
                  </div>
                  <nav className="sidebar-nav">
                      {routes.map((r) => (
                          <NavLink key={r.path} to={r.path} end>
                              {r.label}
                          </NavLink>
                      ))}
                  </nav>
                  <div className="sidebar-footer">`

                  </div>
              </div>
              <div className="page-wrapper">
                  <Routes>
                      {routes.map((r) => (
                          <Route key={r.path} path={r.path} element={r.element} />
                      ))}
                      <Route path="*" element={<Navigate to={routes[0].path} replace />} />
                  </Routes>
              </div>
          </div>
      </BrowserRouter>
  )
}
