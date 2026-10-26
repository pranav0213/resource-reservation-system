
import { useEffect, useState } from 'react'
import './App.css'

function App({ token, role }) {
  const isAdmin = role === 'ADMIN'
  const [activeTab, setActiveTab] = useState('resources')
  const [resources, setResources] = useState([])
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [bookingResource, setBookingResource] = useState(null)
  const [reservedBy, setReservedBy] = useState('')
  const [startTime, setStartTime] = useState('')
  const [endTime, setEndTime] = useState('')
  const [bookingMessage, setBookingMessage] = useState('')
  const [bookingLoading, setBookingLoading] = useState(false)
  const [cancellingId, setCancellingId] = useState(null)
  const [deletingId, setDeletingId] = useState(null)

  const [showResourceForm, setShowResourceForm] = useState(false)
  const [editingResource, setEditingResource] = useState(null)
  const [resourceName, setResourceName] = useState('')
  const [resourceDescription, setResourceDescription] = useState('')
  const [resourceType, setResourceType] = useState('')
  const [resourceAvailable, setResourceAvailable] = useState(true)
  const [resourceSaving, setResourceSaving] = useState(false)
  const [resourceMessage, setResourceMessage] = useState('')

  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

  async function authenticatedFetch(url, options = {}) {
    return fetch(`${API_BASE_URL}${url}`, {
      ...options,
      headers: {
        ...options.headers,
        Authorization: `Bearer ${token}`
      }
    })
  }

  async function loadData() {
    try {
      setError('')

      const [resourceResponse, reservationResponse] =
        await Promise.all([
          authenticatedFetch('/api/resources'),
          authenticatedFetch('/api/reservations')
        ])

      if (!resourceResponse.ok || !reservationResponse.ok) {
        throw new Error('Unable to load data from the server.')
      }

      setResources(await resourceResponse.json())
      setReservations(await reservationResponse.json())
    } catch (err) {
      setError(err.message || 'Unable to load data.')
      throw err
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData().catch(() => {})
  }, [])

  function openBooking(resource) {
    setBookingResource(resource)
    setReservedBy('')
    setStartTime('')
    setEndTime('')
    setBookingMessage('')
  }

  function closeBooking() {
    setBookingResource(null)
    setBookingMessage('')
  }

  async function handleBooking(event) {
    event.preventDefault()

    if (!bookingResource || bookingLoading) return

    setBookingLoading(true)
    setBookingMessage('')

    try {
      const response = await authenticatedFetch('/api/reservations', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          resourceId: bookingResource.id,
          reservedBy: reservedBy.trim(),
          startTime,
          endTime
        })
      })

      if (!response.ok) {
        const data = await response.json().catch(() => ({}))

        throw new Error(
          data.error ||
          Object.values(data).join(', ') ||
          'Reservation failed.'
        )
      }

      closeBooking()
      setActiveTab('reservations')
      await loadData()
    } catch (err) {
      setBookingMessage(err.message || 'Reservation failed.')
    } finally {
      setBookingLoading(false)
    }
  }

  async function handleCancelReservation(id) {
    if (!window.confirm('Cancel this reservation?')) return

    setCancellingId(id)

    try {
      const response = await authenticatedFetch(`/api/reservations/${id}`, {
        method: 'DELETE'
      })

      if (!response.ok) {
        throw new Error('Failed to cancel reservation.')
      }

      await loadData()
      window.alert('Reservation cancelled successfully!')
    } catch (err) {
      window.alert(err.message || 'Unable to cancel reservation.')
    } finally {
      setCancellingId(null)
    }
  }

  async function handleDeleteResource(resource) {
    if (deletingId !== null) return

    const confirmed = window.confirm(
      `Are you sure you want to delete ${resource.name}?`
    )

    if (!confirmed) return

    setDeletingId(resource.id)

    try {
      const response = await authenticatedFetch(`/api/resources/${resource.id}`, {
        method: 'DELETE'
      })

      if (!response.ok) {
        const data = await response.json().catch(() => ({}))

        throw new Error(
          data.error || 'Failed to delete resource.'
        )
      }

      await loadData()
      window.alert('Resource deleted successfully!')
    } catch (err) {
      window.alert(err.message || 'Unable to delete resource.')
    } finally {
      setDeletingId(null)
    }
  }

  function openAddResource() {
    setEditingResource(null)
    setResourceName('')
    setResourceDescription('')
    setResourceType('')
    setResourceAvailable(true)
    setResourceMessage('')
    setShowResourceForm(true)
  }

  function openEditResource(resource) {
    setEditingResource(resource)
    setResourceName(resource.name || '')
    setResourceDescription(resource.description || '')
    setResourceType(resource.type || '')
    setResourceAvailable(resource.available)
    setResourceMessage('')
    setShowResourceForm(true)
  }

  function closeResourceForm() {
    setShowResourceForm(false)
    setEditingResource(null)
    setResourceMessage('')
  }

  async function handleSaveResource(event) {
    event.preventDefault()

    if (resourceSaving) return

    setResourceSaving(true)
    setResourceMessage('')

    try {
      const isEditing = editingResource !== null

      const url = isEditing
        ? `/api/resources/${editingResource.id}`
        : '/api/resources'

      const response = await authenticatedFetch(url, {
        method: isEditing ? 'PUT' : 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          name: resourceName.trim(),
          description: resourceDescription.trim(),
          type: resourceType,
          available: resourceAvailable
        })
      })

      if (!response.ok) {
        const data = await response.json().catch(() => ({}))

        throw new Error(
          data.error ||
          Object.values(data).join(', ') ||
          'Failed to save resource.'
        )
      }

      closeResourceForm()
      await loadData()

      window.alert(
        isEditing
          ? 'Resource updated successfully!'
          : 'Resource added successfully!'
      )
    } catch (err) {
      setResourceMessage(err.message || 'Failed to save resource.')
    } finally {
      setResourceSaving(false)
    }
  }

  return (
    <div className="app">
      <aside className="sidebar">
        <h2>ReserveHub</h2>
        <p>Resource Management</p>

        <nav>
          <button
            className={activeTab === 'resources' ? 'active' : ''}
            onClick={() => setActiveTab('resources')}
          >
            Resources
          </button>

          <button
            className={activeTab === 'reservations' ? 'active' : ''}
            onClick={() => setActiveTab('reservations')}
          >
            Reservations
          </button>
        </nav>
      </aside>

      <main className="main">
        <header>
          <div>
            <h1>Resource Reservation System</h1>
            <p>Manage your resources and bookings in one place.</p>
          </div>
          <span className="status">Dashboard</span>
        </header>

        <section className="stats">
          <div className="stat-card">
            <span>Total Resources</span>
            <h2>{loading ? '...' : error ? '—' : resources.length}</h2>
            <p>Resources in the database</p>
          </div>

          <div className="stat-card">
            <span>Total Reservations</span>
            <h2>{loading ? '...' : error ? '—' : reservations.length}</h2>
            <p>Reservations in the database</p>
          </div>

          <div className="stat-card">
            <span>System Status</span>
            <h2>{error ? 'Error' : loading ? 'Loading' : 'Connected'}</h2>
            <p>Spring Boot API</p>
          </div>
        </section>

        {error && (
          <div className="content-card">
            <p style={{ color: '#dc2626' }}>{error}</p>
          </div>
        )}

        <section className="content-card">
          {activeTab === 'resources' ? (
            <>
              <div className="section-heading">
                <div>
                  <h2>Available Resources</h2>
                  <p>
                    View and manage rooms, equipment, and other resources.
                  </p>
                </div>

                {isAdmin && (
                  <button
                    className="primary-button"
                    onClick={openAddResource}
                  >
                    + Add Resource
                  </button>
                )}
              </div>

              {loading ? (
                <p>Loading resources...</p>
              ) : resources.length === 0 ? (
                <div className="empty-state">
                  {isAdmin ? 'No resources found. Add your first resource!' : 'No resources available.'}
                </div>
              ) : (
                <div className="resource-grid">
                  {resources.map(resource => (
                    <div className="resource-card" key={resource.id}>
                      <h3>{resource.name}</h3>
                      <p>{resource.description}</p>

                      <p>
                        <strong>Type:</strong> {resource.type}
                      </p>

                      <span
                        className={
                          resource.available
                            ? 'availability available'
                            : 'availability unavailable'
                        }
                      >
                        {resource.available ? 'Available' : 'Unavailable'}
                      </span>

                      <div className="card-actions">
                        <button
                          className="primary-button"
                          disabled={!resource.available}
                          onClick={() => openBooking(resource)}
                        >
                          Book Resource
                        </button>

                        {isAdmin && (
                          <>
                            <button
                              className="secondary-button"
                              onClick={() => openEditResource(resource)}
                            >
                              Edit Resource
                            </button>

                            <button
                              className="cancel-button"
                              disabled={deletingId !== null}
                              onClick={() => handleDeleteResource(resource)}
                            >
                              {deletingId === resource.id
                                ? 'Deleting...'
                                : 'Delete Resource'}
                            </button>
                          </>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </>
          ) : (
            <>
              <h2>Reservations</h2>
              <p>View and manage your existing bookings.</p>

              {loading ? (
                <p>Loading reservations...</p>
              ) : reservations.length === 0 ? (
                <div className="empty-state">
                  No reservations found.
                </div>
              ) : (
                <div className="resource-grid">
                  {reservations.map(reservation => (
                    <div className="resource-card" key={reservation.id}>
                      <h3>
                        {reservation.resource?.name || 'Resource'}
                      </h3>

                      <p>
                        <strong>Reserved by:</strong>{' '}
                        {reservation.reservedBy}
                      </p>

                      <p>
                        <strong>Start:</strong>{' '}
                        {reservation.startTime}
                      </p>

                      <p>
                        <strong>End:</strong>{' '}
                        {reservation.endTime}
                      </p>

                      <div className="card-actions">
                        <button
                          className="cancel-button"
                          disabled={cancellingId !== null}
                          onClick={() =>
                            handleCancelReservation(reservation.id)
                          }
                        >
                          {cancellingId === reservation.id
                            ? 'Cancelling...'
                            : 'Cancel Reservation'}
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </>
          )}
        </section>

        {bookingResource && (
          <div className="modal-overlay">
            <div
              className="booking-modal"
              role="dialog"
              aria-modal="true"
              aria-labelledby="booking-title"
            >
              <h2 id="booking-title">Book Resource</h2>

              <p>
                Booking: <strong>{bookingResource.name}</strong>
              </p>

              <form onSubmit={handleBooking}>
                <label htmlFor="reservedBy">Your Name</label>
                <input
                  id="reservedBy"
                  type="text"
                  value={reservedBy}
                  onChange={event => setReservedBy(event.target.value)}
                  placeholder="Enter your name"
                  required
                />

                <label htmlFor="startTime">Start Time</label>
                <input
                  id="startTime"
                  type="datetime-local"
                  value={startTime}
                  onChange={event => setStartTime(event.target.value)}
                  required
                />

                <label htmlFor="endTime">End Time</label>
                <input
                  id="endTime"
                  type="datetime-local"
                  value={endTime}
                  onChange={event => setEndTime(event.target.value)}
                  required
                />

                {bookingMessage && (
                  <p className="booking-error" role="alert">
                    {bookingMessage}
                  </p>
                )}

                <div className="modal-actions">
                  <button
                    type="button"
                    className="secondary-button"
                    onClick={closeBooking}
                    disabled={bookingLoading}
                  >
                    Cancel
                  </button>

                  <button
                    type="submit"
                    className="primary-button"
                    disabled={bookingLoading}
                  >
                    {bookingLoading ? 'Booking...' : 'Confirm Booking'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {showResourceForm && (
          <div className="modal-overlay">
            <div
              className="booking-modal"
              role="dialog"
              aria-modal="true"
              aria-labelledby="resource-form-title"
            >
              <h2 id="resource-form-title">
                {editingResource ? 'Edit Resource' : 'Add New Resource'}
              </h2>

              <form onSubmit={handleSaveResource}>
                <label htmlFor="resourceName">Resource Name</label>
                <input
                  id="resourceName"
                  type="text"
                  value={resourceName}
                  onChange={event => setResourceName(event.target.value)}
                  maxLength={100}
                  required
                />

                <label htmlFor="resourceDescription">Description</label>
                <input
                  id="resourceDescription"
                  type="text"
                  value={resourceDescription}
                  onChange={event => setResourceDescription(event.target.value)}
                />

                <label htmlFor="resourceType">Resource Type</label>
                <select
                  id="resourceType"
                  value={resourceType}
                  onChange={event => setResourceType(event.target.value)}
                  required
                >
                  <option value="">Select a type</option>
                  <option value="ROOM">Room</option>
                  <option value="EQUIPMENT">Equipment</option>
                  <option value="VEHICLE">Vehicle</option>
                  <option value="OTHER">Other</option>
                </select>

                <label htmlFor="resourceAvailable">Availability</label>
                <select
                  id="resourceAvailable"
                  value={resourceAvailable ? 'true' : 'false'}
                  onChange={event =>
                    setResourceAvailable(event.target.value === 'true')
                  }
                >
                  <option value="true">Available</option>
                  <option value="false">Unavailable</option>
                </select>

                {resourceMessage && (
                  <p className="booking-error" role="alert">
                    {resourceMessage}
                  </p>
                )}

                <div className="modal-actions">
                  <button
                    type="button"
                    className="secondary-button"
                    onClick={closeResourceForm}
                    disabled={resourceSaving}
                  >
                    Cancel
                  </button>

                  <button
                    type="submit"
                    className="primary-button"
                    disabled={resourceSaving}
                  >
                    {resourceSaving
                      ? 'Saving...'
                      : editingResource
                        ? 'Save Changes'
                        : 'Add Resource'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </main>
    </div>
  )
}

export default App
