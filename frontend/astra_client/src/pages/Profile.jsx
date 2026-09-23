import { useEffect, useState } from "react";
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  Grid,
  IconButton,
  Stack,
  TextField,
  Typography
} from "@mui/material";

import {
  Add,
  DeleteOutline,
  EditOutlined,
  HomeOutlined,
  LockOutlined,
  PersonOutline,
  PhoneOutlined,
  SaveOutlined,
  Star,
  StarBorder
} from "@mui/icons-material";

import { useAuth } from "../context/AuthContext";
import {
  addressApi,
  userApi
} from "../api/api";


const emptyAddress = {
  fullName: "",
  phone: "",
  addressLine1: "",
  addressLine2: "",
  city: "",
  state: "",
  postalCode: "",
  country: "India"
};


export default function Profile() {
  const {
    user,
    saveAuth
  } = useAuth();

  const [profile, setProfile] =
    useState({
      name: "",
      email: "",
      phone: ""
    });

  const [addresses, setAddresses] =
    useState([]);

  const [loading, setLoading] =
    useState(true);

  const [savingProfile, setSavingProfile] =
    useState(false);

  const [savingPassword, setSavingPassword] =
    useState(false);

  const [savingAddress, setSavingAddress] =
    useState(false);

  const [error, setError] =
    useState("");

  const [success, setSuccess] =
    useState("");

  const [passwords, setPasswords] =
    useState({
      currentPassword: "",
      newPassword: "",
      confirmPassword: ""
    });

  const [addressDialog, setAddressDialog] =
    useState(false);

  const [editingAddress, setEditingAddress] =
    useState(null);

  const [addressForm, setAddressForm] =
    useState(emptyAddress);


  /*
   * Load profile + addresses.
   */
  const loadProfile = async () => {
    setLoading(true);
    setError("");

    try {
      const [
        profileResponse,
        addressResponse
      ] = await Promise.all([
        userApi.me(),
        userApi.addresses()
      ]);

      const profileData =
        profileResponse.data || {};

      const addressData =
        addressResponse.data;

      setProfile({
        name:
          profileData.name ||
          user?.name ||
          "",
        email:
          profileData.email ||
          user?.email ||
          "",
        phone:
          profileData.phone ||
          user?.phone ||
          ""
      });

      const addressList =
        addressData?.addresses ||
        addressData?.content ||
        addressData ||
        [];

      setAddresses(
        Array.isArray(addressList)
          ? addressList
          : []
      );

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        "Unable to load your profile."
      );
    } finally {
      setLoading(false);
    }
  };


  useEffect(() => {
    loadProfile();
  }, []);


  /*
   * Common message helpers.
   */
  const showSuccess = (
    message
  ) => {
    setSuccess(message);

    setTimeout(() => {
      setSuccess("");
    }, 3000);
  };


  const clearMessages = () => {
    setError("");
    setSuccess("");
  };


  /*
   * Profile update.
   */
  const updateProfile = async () => {
    clearMessages();

    if (!profile.name.trim()) {
      setError(
        "Name is required."
      );
      return;
    }

    setSavingProfile(true);

    try {
      const response =
        await userApi.update({
          name: profile.name.trim(),
          phone: profile.phone.trim()
        });

      const updatedUser =
        response.data?.user ||
        response.data;

      /*
       * Keep AuthContext/localStorage
       * synchronized.
       */
      if (
        updatedUser &&
        user
      ) {
        saveAuth({
          ...user,
          ...updatedUser,
          accessToken:
            localStorage.getItem(
              "astra_access_token"
            ),
          refreshToken:
            localStorage.getItem(
              "astra_refresh_token"
            )
        });
      }

      showSuccess(
        "Profile updated successfully."
      );

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        "Unable to update profile."
      );
    } finally {
      setSavingProfile(false);
    }
  };


  /*
   * Password update.
   */
  const updatePassword = async () => {
    clearMessages();

    if (
      !passwords.currentPassword ||
      !passwords.newPassword ||
      !passwords.confirmPassword
    ) {
      setError(
        "Please fill in all password fields."
      );
      return;
    }

    if (
      passwords.newPassword.length < 6
    ) {
      setError(
        "New password must be at least 6 characters."
      );
      return;
    }

    if (
      passwords.newPassword !==
      passwords.confirmPassword
    ) {
      setError(
        "New passwords do not match."
      );
      return;
    }

    setSavingPassword(true);

    try {
      await userApi.password({
        currentPassword:
          passwords.currentPassword,
        newPassword:
          passwords.newPassword
      });

      setPasswords({
        currentPassword: "",
        newPassword: "",
        confirmPassword: ""
      });

      showSuccess(
        "Password changed successfully."
      );

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        "Unable to change password."
      );
    } finally {
      setSavingPassword(false);
    }
  };


  /*
   * Open add address dialog.
   */
  const openAddAddress = () => {
    setEditingAddress(null);

    setAddressForm({
      ...emptyAddress
    });

    setAddressDialog(true);
  };


  /*
   * Open edit address dialog.
   */
  const openEditAddress = (
    address
  ) => {
    setEditingAddress(address);

    setAddressForm({
      fullName:
        address.fullName ||
        address.name ||
        "",
      phone:
        address.phone ||
        "",
      addressLine1:
        address.addressLine1 ||
        address.line1 ||
        address.address ||
        "",
      addressLine2:
        address.addressLine2 ||
        address.line2 ||
        "",
      city:
        address.city ||
        "",
      state:
        address.state ||
        "",
      postalCode:
        address.postalCode ||
        address.zipCode ||
        "",
      country:
        address.country ||
        "India"
    });

    setAddressDialog(true);
  };


  const closeAddressDialog = () => {
    if (savingAddress) {
      return;
    }

    setAddressDialog(false);
    setEditingAddress(null);
    setAddressForm({
      ...emptyAddress
    });
  };


  /*
   * Address form change.
   */
  const changeAddress = (
    field,
    value
  ) => {
    setAddressForm(
      previous => ({
        ...previous,
        [field]: value
      })
    );
  };


  /*
   * Save address.
   */
  const saveAddress = async () => {
    clearMessages();

    if (
      !addressForm.fullName.trim() ||
      !addressForm.phone.trim() ||
      !addressForm.addressLine1.trim() ||
      !addressForm.city.trim() ||
      !addressForm.state.trim() ||
      !addressForm.postalCode.trim()
    ) {
      setError(
        "Please fill in all required address fields."
      );
      return;
    }

    setSavingAddress(true);

    try {
      if (editingAddress) {
        await addressApi.update(
          editingAddress.id,
          addressForm
        );

        showSuccess(
          "Address updated successfully."
        );
      } else {
        await addressApi.create(
          addressForm
        );

        showSuccess(
          "Address added successfully."
        );
      }

      closeAddressDialog();

      const response =
        await userApi.addresses();

      const data =
        response.data;

      const list =
        data?.addresses ||
        data?.content ||
        data ||
        [];

      setAddresses(
        Array.isArray(list)
          ? list
          : []
      );

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        "Unable to save address."
      );
    } finally {
      setSavingAddress(false);
    }
  };


  /*
   * Delete address.
   */
  const deleteAddress = async (
    address
  ) => {
    const confirmed =
      window.confirm(
        "Are you sure you want to delete this address?"
      );

    if (!confirmed) {
      return;
    }

    clearMessages();

    try {
      await addressApi.remove(
        address.id
      );

      setAddresses(
        previous =>
          previous.filter(
            item =>
              item.id !==
              address.id
          )
      );

      showSuccess(
        "Address deleted successfully."
      );

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        "Unable to delete address."
      );
    }
  };


  /*
   * Set default address.
   */
  const setDefaultAddress = async (
    address
  ) => {
    clearMessages();

    try {
      await addressApi.setDefault(
        address.id
      );

      setAddresses(
        previous =>
          previous.map(
            item => ({
              ...item,
              isDefault:
                item.id ===
                address.id,
              default:
                item.id ===
                address.id
            })
          )
      );

      showSuccess(
        "Default address updated."
      );

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        "Unable to set default address."
      );
    }
  };


  const isDefaultAddress = (
    address
  ) =>
    Boolean(
      address.isDefault ||
      address.default ||
      address.defaultAddress
    );


  /*
   * Loading state.
   */
  if (loading) {
    return (
      <Container
        maxWidth="lg"
        sx={{
          py: 10
        }}
      >
        <Stack
          alignItems="center"
          spacing={2}
        >
          <Typography
            color="text.secondary"
          >
            Loading your profile...
          </Typography>
        </Stack>
      </Container>
    );
  }


  return (
    <Container
      maxWidth="lg"
      sx={{
        py: {
          xs: 3,
          md: 5
        }
      }}
    >

      {/* Page header */}
      <Box sx={{ mb: 4 }}>
        <Typography
          variant="h4"
          fontWeight={900}
        >
          My Profile
        </Typography>

        <Typography
          color="text.secondary"
          sx={{
            mt: 0.75
          }}
        >
          Manage your personal
          information, password
          and delivery addresses.
        </Typography>
      </Box>


      {/* Alerts */}
      {error && (
        <Alert
          severity="error"
          sx={{
            mb: 3
          }}
          onClose={() =>
            setError("")
          }
        >
          {error}
        </Alert>
      )}

      {success && (
        <Alert
          severity="success"
          sx={{
            mb: 3
          }}
          onClose={() =>
            setSuccess("")
          }
        >
          {success}
        </Alert>
      )}


      <Grid
        container
        spacing={3}
      >

        {/* Personal information */}
        <Grid
          size={{
            xs: 12,
            md: 7
          }}
        >
          <Card>
            <CardContent
              sx={{
                p: {
                  xs: 2.5,
                  md: 3
                }
              }}
            >
              <Stack
                direction="row"
                spacing={1}
                alignItems="center"
                mb={3}
              >
                <PersonOutline
                  color="primary"
                />

                <Box>
                  <Typography
                    variant="h6"
                    fontWeight={800}
                  >
                    Personal Information
                  </Typography>

                  <Typography
                    variant="body2"
                    color="text.secondary"
                  >
                    Update your account
                    details.
                  </Typography>
                </Box>
              </Stack>


              <Stack spacing={2.5}>

                <TextField
                  label="Full Name"
                  value={
                    profile.name
                  }
                  onChange={event =>
                    setProfile(
                      previous => ({
                        ...previous,
                        name:
                          event.target
                            .value
                      })
                    )
                  }
                  fullWidth
                  required
                  InputProps={{
                    startAdornment: (
                      <PersonOutline
                        sx={{
                          mr: 1,
                          color:
                            "text.secondary"
                        }}
                      />
                    )
                  }}
                />


                <TextField
                  label="Email Address"
                  value={
                    profile.email
                  }
                  fullWidth
                  disabled
                  helperText="Email address cannot be changed here."
                />


                <TextField
                  label="Phone Number"
                  value={
                    profile.phone
                  }
                  onChange={event =>
                    setProfile(
                      previous => ({
                        ...previous,
                        phone:
                          event.target
                            .value
                      })
                    )
                  }
                  fullWidth
                  InputProps={{
                    startAdornment: (
                      <PhoneOutlined
                        sx={{
                          mr: 1,
                          color:
                            "text.secondary"
                        }}
                      />
                    )
                  }}
                />


                <Button
                  variant="contained"
                  size="large"
                  startIcon={
                    <SaveOutlined />
                  }
                  onClick={
                    updateProfile
                  }
                  disabled={
                    savingProfile
                  }
                  sx={{
                    alignSelf: {
                      xs: "stretch",
                      sm: "flex-start"
                    }
                  }}
                >
                  {savingProfile
                    ? "Saving..."
                    : "Save Changes"}
                </Button>

              </Stack>
            </CardContent>
          </Card>
        </Grid>


        {/* Account summary */}
        <Grid
          size={{
            xs: 12,
            md: 5
          }}
        >
          <Card
            sx={{
              height: "100%"
            }}
          >
            <CardContent
              sx={{
                p: {
                  xs: 2.5,
                  md: 3
                }
              }}
            >
              <Typography
                variant="h6"
                fontWeight={800}
                mb={2}
              >
                Account
              </Typography>

              <Box
                sx={{
                  p: 2,
                  borderRadius: 2,
                  bgcolor:
                    "primary.main",
                  color: "primary.contrastText"
                }}
              >
                <Typography
                  variant="h5"
                  fontWeight={900}
                >
                  {profile.name ||
                    "ASTRA Customer"}
                </Typography>

                <Typography
                  sx={{
                    opacity: 0.85,
                    mt: 0.5
                  }}
                >
                  {profile.email}
                </Typography>

                {user?.role && (
                  <Box
                    sx={{
                      mt: 2,
                      display:
                        "inline-flex",
                      px: 1.5,
                      py: 0.5,
                      borderRadius: 10,
                      bgcolor:
                        "rgba(255,255,255,0.18)"
                    }}
                  >
                    <Typography
                      variant="caption"
                      fontWeight={800}
                    >
                      {user.role}
                    </Typography>
                  </Box>
                )}
              </Box>


              <Stack
                spacing={2}
                sx={{
                  mt: 3
                }}
              >
                <Stack
                  direction="row"
                  spacing={1.5}
                  alignItems="center"
                >
                  <HomeOutlined
                    color="primary"
                  />

                  <Box>
                    <Typography
                      variant="body2"
                      color="text.secondary"
                    >
                      Saved Addresses
                    </Typography>

                    <Typography
                      fontWeight={800}
                    >
                      {
                        addresses.length
                      }
                    </Typography>
                  </Box>
                </Stack>

                <Divider />

                <Stack
                  direction="row"
                  spacing={1.5}
                  alignItems="center"
                >
                  <LockOutlined
                    color="primary"
                  />

                  <Box>
                    <Typography
                      variant="body2"
                      color="text.secondary"
                    >
                      Password
                    </Typography>

                    <Typography
                      fontWeight={800}
                    >
                      Protected
                    </Typography>
                  </Box>
                </Stack>
              </Stack>
            </CardContent>
          </Card>
        </Grid>


        {/* Password */}
        <Grid
          size={{
            xs: 12,
            md: 7
          }}
        >
          <Card>
            <CardContent
              sx={{
                p: {
                  xs: 2.5,
                  md: 3
                }
              }}
            >
              <Stack
                direction="row"
                spacing={1}
                alignItems="center"
                mb={3}
              >
                <LockOutlined
                  color="primary"
                />

                <Box>
                  <Typography
                    variant="h6"
                    fontWeight={800}
                  >
                    Change Password
                  </Typography>

                  <Typography
                    variant="body2"
                    color="text.secondary"
                  >
                    Keep your account
                    secure.
                  </Typography>
                </Box>
              </Stack>


              <Stack spacing={2.5}>

                <TextField
                  label="Current Password"
                  type="password"
                  value={
                    passwords.currentPassword
                  }
                  onChange={event =>
                    setPasswords(
                      previous => ({
                        ...previous,
                        currentPassword:
                          event.target
                            .value
                      })
                    )
                  }
                  fullWidth
                />


                <TextField
                  label="New Password"
                  type="password"
                  value={
                    passwords.newPassword
                  }
                  onChange={event =>
                    setPasswords(
                      previous => ({
                        ...previous,
                        newPassword:
                          event.target
                            .value
                      })
                    )
                  }
                  fullWidth
                  helperText="Minimum 6 characters."
                />


                <TextField
                  label="Confirm New Password"
                  type="password"
                  value={
                    passwords.confirmPassword
                  }
                  onChange={event =>
                    setPasswords(
                      previous => ({
                        ...previous,
                        confirmPassword:
                          event.target
                            .value
                      })
                    )
                  }
                  fullWidth
                />


                <Button
                  variant="outlined"
                  size="large"
                  startIcon={
                    <LockOutlined />
                  }
                  onClick={
                    updatePassword
                  }
                  disabled={
                    savingPassword
                  }
                  sx={{
                    alignSelf: {
                      xs: "stretch",
                      sm: "flex-start"
                    }
                  }}
                >
                  {savingPassword
                    ? "Changing..."
                    : "Change Password"}
                </Button>

              </Stack>
            </CardContent>
          </Card>
        </Grid>


        {/* Addresses */}
        <Grid
          size={12}
        >
          <Card>
            <CardContent
              sx={{
                p: {
                  xs: 2.5,
                  md: 3
                }
              }}
            >

              <Stack
                direction={{
                  xs: "column",
                  sm: "row"
                }}
                justifyContent="space-between"
                alignItems={{
                  xs: "flex-start",
                  sm: "center"
                }}
                spacing={2}
                mb={3}
              >

                <Stack
                  direction="row"
                  spacing={1}
                  alignItems="center"
                >
                  <HomeOutlined
                    color="primary"
                  />

                  <Box>
                    <Typography
                      variant="h6"
                      fontWeight={800}
                    >
                      Saved Addresses
                    </Typography>

                    <Typography
                      variant="body2"
                      color="text.secondary"
                    >
                      Manage your delivery
                      addresses.
                    </Typography>
                  </Box>
                </Stack>


                <Button
                  variant="contained"
                  startIcon={
                    <Add />
                  }
                  onClick={
                    openAddAddress
                  }
                >
                  Add Address
                </Button>

              </Stack>


              {addresses.length ===
              0 ? (
                <Box
                  sx={{
                    py: 6,
                    textAlign:
                      "center",
                    border:
                      "1px dashed",
                    borderColor:
                      "divider",
                    borderRadius: 2
                  }}
                >
                  <HomeOutlined
                    sx={{
                      fontSize: 48,
                      color:
                        "text.disabled",
                      mb: 1
                    }}
                  />

                  <Typography
                    variant="h6"
                    fontWeight={700}
                  >
                    No saved addresses
                  </Typography>

                  <Typography
                    color="text.secondary"
                    sx={{
                      mt: 0.5,
                      mb: 2
                    }}
                  >
                    Add an address for
                    faster checkout.
                  </Typography>

                  <Button
                    variant="outlined"
                    startIcon={
                      <Add />
                    }
                    onClick={
                      openAddAddress
                    }
                  >
                    Add Your First Address
                  </Button>
                </Box>
              ) : (
                <Grid
                  container
                  spacing={2}
                >
                  {addresses.map(
                    address => (
                      <Grid
                        key={
                          address.id
                        }
                        size={{
                          xs: 12,
                          md: 6
                        }}
                      >
                        <Card
                          variant="outlined"
                          sx={{
                            height:
                              "100%",
                            position:
                              "relative",
                            borderColor:
                              isDefaultAddress(
                                address
                              )
                                ? "primary.main"
                                : "divider",
                            borderWidth:
                              isDefaultAddress(
                                address
                              )
                                ? 2
                                : 1
                          }}
                        >
                          <CardContent>
                            <Stack
                              direction="row"
                              justifyContent="space-between"
                              alignItems="flex-start"
                              spacing={2}
                            >
                              <Box>
                                <Stack
                                  direction="row"
                                  spacing={1}
                                  alignItems="center"
                                >
                                  <Typography
                                    fontWeight={
                                      800
                                    }
                                  >
                                    {address.fullName ||
                                      address.name ||
                                      "Address"}
                                  </Typography>

                                  {isDefaultAddress(
                                    address
                                  ) && (
                                    <Typography
                                      variant="caption"
                                      fontWeight={
                                        800
                                      }
                                      color="primary"
                                    >
                                      DEFAULT
                                    </Typography>
                                  )}
                                </Stack>

                                <Typography
                                  variant="body2"
                                  color="text.secondary"
                                  sx={{
                                    mt: 0.5
                                  }}
                                >
                                  {address.phone}
                                </Typography>
                              </Box>

                              <Stack
                                direction="row"
                              >
                                <IconButton
                                  size="small"
                                  color="primary"
                                  onClick={() =>
                                    openEditAddress(
                                      address
                                    )
                                  }
                                  aria-label="Edit address"
                                >
                                  <EditOutlined />
                                </IconButton>

                                <IconButton
                                  size="small"
                                  color="error"
                                  onClick={() =>
                                    deleteAddress(
                                      address
                                    )
                                  }
                                  aria-label="Delete address"
                                >
                                  <DeleteOutline />
                                </IconButton>
                              </Stack>
                            </Stack>


                            <Typography
                              variant="body2"
                              sx={{
                                mt: 2,
                                lineHeight:
                                  1.7
                              }}
                            >
                              {address.addressLine1 ||
                                address.line1 ||
                                address.address}

                              {(address.addressLine2 ||
                                address.line2) && (
                                <>
                                  <br />
                                  {address.addressLine2 ||
                                    address.line2}
                                </>
                              )}

                              <br />

                              {address.city},{" "}
                              {address.state}{" "}
                              {address.postalCode ||
                                address.zipCode}

                              <br />

                              {address.country ||
                                "India"}
                            </Typography>


                            {!isDefaultAddress(
                              address
                            ) && (
                              <Button
                                size="small"
                                startIcon={
                                  <StarBorder />
                                }
                                sx={{
                                  mt: 2
                                }}
                                onClick={() =>
                                  setDefaultAddress(
                                    address
                                  )
                                }
                              >
                                Set as Default
                              </Button>
                            )}

                            {isDefaultAddress(
                              address
                            ) && (
                              <Button
                                size="small"
                                startIcon={
                                  <Star />
                                }
                                sx={{
                                  mt: 2
                                }}
                                disabled
                              >
                                Default Address
                              </Button>
                            )}

                          </CardContent>
                        </Card>
                      </Grid>
                    )
                  )}
                </Grid>
              )}

            </CardContent>
          </Card>
        </Grid>

      </Grid>


      {/* Address dialog */}
      <Dialog
        open={addressDialog}
        onClose={
          closeAddressDialog
        }
        fullWidth
        maxWidth="sm"
      >

        <DialogTitle>
          {editingAddress
            ? "Edit Address"
            : "Add New Address"}
        </DialogTitle>


        <DialogContent>
          <Stack
            spacing={2}
            sx={{
              pt: 1
            }}
          >

            <TextField
              label="Full Name"
              value={
                addressForm.fullName
              }
              onChange={event =>
                changeAddress(
                  "fullName",
                  event.target
                    .value
                )
              }
              fullWidth
              required
            />


            <TextField
              label="Phone Number"
              value={
                addressForm.phone
              }
              onChange={event =>
                changeAddress(
                  "phone",
                  event.target
                    .value
                )
              }
              fullWidth
              required
            />


            <TextField
              label="Address Line 1"
              value={
                addressForm.addressLine1
              }
              onChange={event =>
                changeAddress(
                  "addressLine1",
                  event.target
                    .value
                )
              }
              fullWidth
              required
              placeholder="House / Flat / Street"
            />


            <TextField
              label="Address Line 2"
              value={
                addressForm.addressLine2
              }
              onChange={event =>
                changeAddress(
                  "addressLine2",
                  event.target
                    .value
                )
              }
              fullWidth
              placeholder="Apartment, landmark, etc."
            />


            <Grid
              container
              spacing={2}
            >

              <Grid
                size={{
                  xs: 12,
                  sm: 6
                }}
              >
                <TextField
                  label="City"
                  value={
                    addressForm.city
                  }
                  onChange={event =>
                    changeAddress(
                      "city",
                      event.target
                        .value
                    )
                  }
                  fullWidth
                  required
                />
              </Grid>


              <Grid
                size={{
                  xs: 12,
                  sm: 6
                }}
              >
                <TextField
                  label="State"
                  value={
                    addressForm.state
                  }
                  onChange={event =>
                    changeAddress(
                      "state",
                      event.target
                        .value
                    )
                  }
                  fullWidth
                  required
                />
              </Grid>


              <Grid
                size={{
                  xs: 12,
                  sm: 6
                }}
              >
                <TextField
                  label="Postal Code"
                  value={
                    addressForm.postalCode
                  }
                  onChange={event =>
                    changeAddress(
                      "postalCode",
                      event.target
                        .value
                    )
                  }
                  fullWidth
                  required
                />
              </Grid>


              <Grid
                size={{
                  xs: 12,
                  sm: 6
                }}
              >
                <TextField
                  label="Country"
                  value={
                    addressForm.country
                  }
                  onChange={event =>
                    changeAddress(
                      "country",
                      event.target
                        .value
                    )
                  }
                  fullWidth
                />
              </Grid>

            </Grid>

          </Stack>
        </DialogContent>


        <DialogActions
          sx={{
            px: 3,
            pb: 2
          }}
        >
          <Button
            onClick={
              closeAddressDialog
            }
            disabled={
              savingAddress
            }
          >
            Cancel
          </Button>

          <Button
            variant="contained"
            startIcon={
              <SaveOutlined />
            }
            onClick={
              saveAddress
            }
            disabled={
              savingAddress
            }
          >
            {savingAddress
              ? "Saving..."
              : editingAddress
                ? "Update Address"
                : "Save Address"}
          </Button>
        </DialogActions>

      </Dialog>

    </Container>
  );
}
